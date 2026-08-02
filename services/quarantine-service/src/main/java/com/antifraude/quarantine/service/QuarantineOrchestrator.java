package com.antifraude.quarantine.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.antifraude.quarantine.client.GameBackendClient;

import com.antifraude.quarantine.domain.QuarantineStatus;
import com.antifraude.quarantine.event.QuarantineUpdatedEvent;
import com.antifraude.quarantine.event.ScoreUpdatedEvent;
import com.antifraude.quarantine.model.QuarantineRecord;
import com.antifraude.quarantine.readmodel.QuarantineProjectionService;
import com.antifraude.quarantine.repository.QuarantineRepository;


@Service
public class QuarantineOrchestrator {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(QuarantineOrchestrator.class);

    private final QuarantineRepository repository;
    private final KafkaTemplate<String, QuarantineUpdatedEvent> kafkaTemplate;
    private final GameBackendClient gameBackendClient;
    private final QuarantineProjectionService projectionService;
    private final String outputTopic;

    /**
     * Cria o orquestrador da SAGA de quarentena.
     *
     * @param repository Repositório de persistência dos registros de quarentena.
     * @param kafkaTemplate Template para publicação de eventos no Kafka.
     * @param gameBackendClient Cliente do backend do jogo, usado para bloquear/desbloquear jogadores.
     * @param projectionService Serviço responsável por atualizar o read model em Redis.
     * @param outputTopic Tópico Kafka de saída para eventos de atualização de quarentena.
     */
    public QuarantineOrchestrator(
            final QuarantineRepository repository,
            final KafkaTemplate<String, QuarantineUpdatedEvent> kafkaTemplate,
            final GameBackendClient gameBackendClient,
            final QuarantineProjectionService projectionService,
            @Value("${quarantine.topics.output-scores:quarantine-updated}") final String outputTopic) {

        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.gameBackendClient = gameBackendClient;
        this.projectionService = projectionService;
        this.outputTopic = outputTopic;
    }

    /**
     * Inicia o fluxo da SAGA de quarentena: registra PENDING, bloqueia o
     * jogador no backend do jogo, marca QUARANTINED e publica o evento.
     * Caso qualquer etapa após o registro falhe, a SAGA executa a
     * compensação e encerra em um estado terminal (FAILED/COMPENSATED),
     * nunca deixando o registro preso em PENDING.
     *
     * @param event Evento recebido do Risk Scoring.
     */
    public void handle(final ScoreUpdatedEvent event) {

        LOGGER.info(
                "Recebido score {} para o jogador {}.",
                event.totalScore(),
                event.playerId()
        );

        //Garante idempotência: verifica se o evento já foi processado
        if (alreadyProcessed(event.eventId())) {
            LOGGER.warn(
                "Evento {} já processado anteriormente.",
                event.eventId()
            );
            return;
        }

        // Verificando threshold
        if (shouldQuarantine(event)) {
            LOGGER.info(
                    "Jogador {} atingiu o threshold de quarentena.",
                    event.playerId());
        } else {
            LOGGER.info(
                    "Jogador {} não atingiu o threshold de quarentena.",
                    event.playerId()
            );
            return;
        }

        //Criando e registrando um evento de quarentena como PENDING
        QuarantineRecord record = registerPendingQuarantine(event);
        projectionService.project(record);

        //Chamando o backend do jogo para bloquear o jogador
        try {
            blockPlayer(record);
        } catch (final RuntimeException ex) {
            handleFailure(record, ex);
            return;
        }

        //Atualizando quarentena para QUARANTINED
        record = updateStatus(record, QuarantineStatus.QUARANTINED, record.getReason());
        projectionService.project(record);

        //publica evento de quarentena no Kafka
        publishQuarantineEvent(record);
    }

    /**
     * Verifica se o evento já foi processado.
     *
     * @param eventId ID do evento.
     * @return true se o evento já foi processado, false caso contrário.
    */
    private boolean alreadyProcessed(final UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    /**
     * Verifica se o evento deve ser colocado em quarentena.
     *
     * @param event Evento de atualização de score.
     * @return true se o evento deve ser colocado em quarentena, false caso contrário.
    */
    private boolean shouldQuarantine(final ScoreUpdatedEvent event) {
        return event.totalScore() >= event.quarantineThreshold();
    }

    /**
     * Cria o registro inicial da quarentena.
    */
    private QuarantineRecord createPendingRecord(
            final ScoreUpdatedEvent event) {

        return new QuarantineRecord(
                UUID.randomUUID(),
                event.playerId(),
                event.eventId(),
                event.totalScore(),
                QuarantineStatus.PENDING,
                "TotalScore acima do threshold.",
                Instant.now(),
                null
        );
    }

    /**
     * Registra uma quarentena em PENDING.
     *
     * @param event Evento de atualização de score.
     * @return Registro da quarentena.
     */
    private QuarantineRecord registerPendingQuarantine(final ScoreUpdatedEvent event) {
        QuarantineRecord record = createPendingRecord(event);

        repository.save(record);

        LOGGER.info(
            "Quarentena registrada em PENDING para jogador {}.",
            event.playerId());

        return record;
    }

    /**
    * Atualiza o status de uma quarentena para um valor informado, permitindo
    * também atualizar o motivo (por exemplo, ao registrar uma falha).
    *
    * @param record Registro da quarentena.
    * @param status Status a ser atualizado.
    * @param reason Motivo associado ao novo status.
    * @return Registro atualizado e persistido.
    */
    private QuarantineRecord updateStatus(
        final QuarantineRecord record,
        final QuarantineStatus status,
        final String reason) {

        QuarantineRecord updated = new QuarantineRecord(
            record.getId(),
            record.getPlayerId(),
            record.getEventId(),
            record.getTotalScore(),
            status,
            reason,
            record.getCreatedAt(),
            status == QuarantineStatus.PENDING ? null : Instant.now()
        );

        repository.save(updated);

        LOGGER.info(
            "Status da quarentena do jogador {} alterado para {}.",
            record.getPlayerId(),
            status);

        return updated;
    }

    /**
     * Bloqueia um jogador.
     *
     * @param record Registro da quarentena.
     */
    private void blockPlayer(final QuarantineRecord record) {
        gameBackendClient.blockPlayer(record.getPlayerId());
    }

    /**
     * Trata a falha de uma etapa da SAGA posterior ao registro PENDING:
     * marca o registro como FAILED, executa a compensação e publica o
     * estado terminal resultante.
     *
     * @param record Registro da quarentena que estava em andamento.
     * @param cause  Exceção que causou a falha.
     */
    private void handleFailure(final QuarantineRecord record, final RuntimeException cause) {

        LOGGER.error(
            "Falha ao processar quarentena do jogador {}. Iniciando compensação.",
            record.getPlayerId(),
            cause
        );

        QuarantineRecord failed = updateStatus(record, QuarantineStatus.FAILED, cause.getMessage());
        projectionService.project(failed);

        QuarantineRecord compensated = compensate(failed);
        projectionService.project(compensated);

        publishQuarantineEvent(compensated);
    }

    /**
     * Executa a compensação da SAGA: garante que o jogador não fique
     * bloqueado indevidamente e leva o registro ao estado terminal
     * COMPENSATED.
     *
     * @param record Registro da quarentena já marcado como FAILED.
     * @return Registro atualizado para COMPENSATED.
     */
    private QuarantineRecord compensate(final QuarantineRecord record) {

        LOGGER.warn(
            "Executando compensação da quarentena do jogador {}.",
            record.getPlayerId()
        );

        try {
            gameBackendClient.unblockPlayer(record.getPlayerId());
        } catch (final RuntimeException ex) {
            LOGGER.error(
                "Falha ao desbloquear jogador {} durante a compensação.",
                record.getPlayerId(),
                ex
            );
        }

        return updateStatus(record, QuarantineStatus.COMPENSATED, record.getReason());
    }

    /**
    * Constrói o evento de atualização da quarentena para publicação.
    *
    * @param record Registro persistido.
    * @return Evento para publicação no Kafka.
    */
    private QuarantineUpdatedEvent createUpdatedEvent(
        final QuarantineRecord record) {

        return new QuarantineUpdatedEvent(
            record.getPlayerId(),
            record.getEventId(),
            record.getStatus(),
            record.getReason(),
            record.getResolvedAt()
        );
    }

    /**
     * Publica a atualização da quarentena no Kafka.
     *
     * @param record Registro atualizado.
    */
    private void publishQuarantineEvent(
        final QuarantineRecord record) {

        QuarantineUpdatedEvent event =
            createUpdatedEvent(record);

        kafkaTemplate.send(
            outputTopic,
            record.getPlayerId(),
            event);

        LOGGER.info(
            "Evento de quarentena publicado para jogador {}.",
            record.getPlayerId());
    }

}
