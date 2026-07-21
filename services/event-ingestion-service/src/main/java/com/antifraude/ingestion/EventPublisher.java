package com.antifraude.ingestion;

import com.antifraude.ingestion.model.GameEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsTopic;

    public EventPublisher(
            final KafkaTemplate<String, String> kafkaTemplate,
            final ObjectMapper objectMapper,
            @Value("${ingestion.events-topic}") final String eventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.eventsTopic = eventsTopic;
    }

    public void publish(final GameEvent event) {
        String eventPayload = serializeEvent(event);

        // eventId como key ajuda particionamento estavel e ordenacao por chave no Kafka.
        kafkaTemplate.send(eventsTopic, event.eventId().toString(), eventPayload);
    }

    private String serializeEvent(final GameEvent event) {
        try {
            // Persiste um contrato de mensagem explicito (JSON) para desacoplar produtores/consumidores.
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Nao foi possivel serializar o evento para publicacao no Kafka.", ex);
        }
    }
}
