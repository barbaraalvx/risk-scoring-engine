package com.antifraude.quarantine.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.antifraude.quarantine.service.QuarantineOrchestrator;

//Classe responsável por consumir eventos de score atualizado do Risk Scoring
//e encaminhá-los para o QuarantineOrchestrator para processamento.
@Component
public class ScoreUpdatedConsumer {

    private final QuarantineOrchestrator orchestrator;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ScoreUpdatedConsumer.class);


    /**
     * Cria o consumidor de eventos de score.
     *
     * @param orchestrator Orquestrador da SAGA de quarentena.
     */
    public ScoreUpdatedConsumer(final QuarantineOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Consome eventos de score para a quarentena. Usa retry não bloqueante
     * do spring-kafka: em caso de falha, tenta novamente algumas vezes com
     * backoff e, se persistir, envia a mensagem para o dead-letter topic
     * (DLT) em vez de travar a partição com uma "poison pill".
     *
     * @param event Evento de score recebido do Risk Scoring.
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        autoCreateTopics = "true"
    )
    @KafkaListener(
        topics = "${quarantine.topics.input-events:risk-score-updated}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(final ScoreUpdatedEvent event) {
        LOGGER.info(
            "Evento de score recebido para jogador {}.",
            event.playerId()
        );

        //Envia o evento para o QuarantineOrchestrator para processamento
        try {
            orchestrator.handle(event);
        } catch (final RuntimeException ex) {
            LOGGER.error(
                "Erro ao processar evento {}.",
                event.eventId(), ex
            );
            throw ex;
        }
    }

    /**
     * Trata mensagens que esgotaram as tentativas de retry e caíram no
     * dead-letter topic, apenas registrando o ocorrido para investigação
     * manual posterior.
     *
     * @param record Registro Kafka original que falhou definitivamente.
     */
    @DltHandler
    public void handleDlt(final ConsumerRecord<String, ScoreUpdatedEvent> record) {
        LOGGER.error(
            "Evento {} enviado ao dead-letter topic após esgotar as tentativas de retry.",
            record.value() != null ? record.value().eventId() : "desconhecido"
        );
    }
}
