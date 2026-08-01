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
import com.antifraude.quarantine.repository.QuarantineRepository;


@Service
public class QuarantineOrchestrator {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(QuarantineOrchestrator.class);

    private final QuarantineRepository repository;
    private final KafkaTemplate<String, QuarantineUpdatedEvent> kafkaTemplate;
    private final String outputTopic;

    public QuarantineOrchestrator(
            final QuarantineRepository repository,
            final KafkaTemplate<String, QuarantineUpdatedEvent> kafkaTemplate,
            @Value("${quarantine.topics.output-scores:quarantine-updated}") final String outputTopic) {

        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.outputTopic = outputTopic;
    }

    /**
     * Inicia o fluxo da SAGA de quarentena.
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
        if (alreadyProcessed(event.eventId())){
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
        }
        else {
            LOGGER.info(
                    "Jogador {} não atingiu o threshold de quarentena.",
                    event.playerId()
            );
            return;
        }

        //Criando e registrando um evento de quarentena como PENDING
        QuarantineRecord record = registerPendingQuarantine(event);

        //Chamando o backend do jogo para bloquear o jogador
        blockPlayer(record);

        //Atualizando quarentena para QUARANTINED
        record = updateStatus(record, QuarantineStatus.QUARANTINED);

        //publica evento de quarentena no Kafka
        publishQuarantineEvent(record); 

        //compensate(record); //implementação futura
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
    * Atualiza o status de uma quarentena para um valor infomrado.
    *
    * @param record Registro da quarentena.
    * @param status Status a ser atualizado.
    */
    private QuarantineRecord updateStatus(
        final QuarantineRecord record,
        final QuarantineStatus status) {

        QuarantineRecord updated = new QuarantineRecord(
            record.getId(),
            record.getPlayerId(),
            record.getEventId(),
            record.getTotalScore(),
            status,
            record.getReason(),
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
    private void blockPlayer(final QuarantineRecord record){
        GameBackendClient gameBackendClient = new GameBackendClient();
        gameBackendClient.blockPlayer(record.getPlayerId());
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

    //metodos a serem implementados futuramente
    // private void compensate(final QuarantineRecord record)

}