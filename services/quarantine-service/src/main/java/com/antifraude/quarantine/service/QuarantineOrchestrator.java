package com.antifraude.quarantine.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    // Esse método será implementado futuramente
    // private final GameBackendClient gameBackendClient;

    private final KafkaTemplate<String, QuarantineUpdatedEvent> kafkaTemplate;

    public QuarantineOrchestrator(
            final QuarantineRepository repository,
            final KafkaTemplate<String, QuarantineUpdatedEvent> kafkaTemplate) {

        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
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
                event.playerId());

        // Verificando threshold
        if (event.totalScore() < event.quarantineThreshold()) {

            LOGGER.info(
                    "Jogador {} não atingiu o threshold de quarentena.",
                    event.playerId());

            return;
        }

        // Registrando quarentena como PENDING
        QuarantineRecord record = createPendingRecord(event);

        repository.save(record);

        LOGGER.info(
                "Quarentena registrada em PENDING para jogador {}.",
                event.playerId()
        );

        // blockPlayer(record); //implementação futura

        // Atualizando quarentena para QUARANTINED
        record = updateStatus(record, QuarantineStatus.QUARANTINED);

        LOGGER.info(
            "Jogador {} marcado como QUARANTINED.", record.getPlayerId()
        );

        //publishQuarantineEvent(record); //publica evento de quarentena

        //compensate(record); //implementação futura
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

    //metodos a serem implementados futuramente

    // private void blockPlayer(final QuarantineRecord record)
    // private void publishQuarantineEvent(final QuarantineRecord record)
    // private void compensate(final QuarantineRecord record)

}