package com.antifraude.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.antifraude.ingestion.model.GameEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Publicador de eventos de jogador no Kafka.
 * Particiona mensagens por playerId conforme exigido pela especificação.
 */
@Service
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsTopic;

    /**
     * Construtor injetando dependências.
     *
     * @param kafkaTemplate Template Kafka.
     * @param objectMapper  Mapper JSON.
     * @param eventsTopic   Tópico de destino.
     */
    public EventPublisher(
            final KafkaTemplate<String, String> kafkaTemplate,
            final ObjectMapper objectMapper,
            @Value("${ingestion.events-topic:player-actions}") final String eventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.eventsTopic = eventsTopic;
    }

    /**
     * Publica o evento no Kafka usand `playerId` como partição estática.
     *
     * @param event Evento do jogador.
     */
    public void publish(final GameEvent event) {
        String eventPayload = serializeEvent(event);
        // Usa playerId como chave de partição para manter ordenação estrita das ações do mesmo jogador.
        kafkaTemplate.send(eventsTopic, event.playerId(), eventPayload);
    }

    private String serializeEvent(final GameEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Não foi possível serializar o evento para o Kafka.", ex);
        }
    }
}
