package com.antifraude.quarantine.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.antifraude.quarantine.service.QuarantineOrchestrator;

//Classe responsável por consumir eventos de score atualizado do Risk Scoring e encaminhá-los para o QuarantineOrchestrator para processamento.
@Component
public class ScoreUpdatedConsumer {

    private final QuarantineOrchestrator orchestrator;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ScoreUpdatedConsumer.class);
    

    public ScoreUpdatedConsumer(QuarantineOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    // Consumidor Kafka de eventos de score para a Quarentena
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
        } catch (Exception ex) {
            LOGGER.error(
                "Erro ao processar evento {}.",
                event.eventId(), ex
            );
            throw ex;
        }
    }
}
