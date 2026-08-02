package com.antifraude.ingestion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antifraude.ingestion.domain.PlayerEventEntity;
import com.antifraude.ingestion.model.GameEvent;
import com.antifraude.ingestion.repository.PlayerEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serviço responsável por orquestrar a gravação no Event Store (Postgres)
 * e publicação assíncrona no Kafka.
 */
@Service
public class IngestionService {

    private final PlayerEventRepository repository;
    private final EventPublisher publisher;
    private final ObjectMapper objectMapper;

    /**
     * Construtor injetando dependências.
     *
     * @param repository Repositório PostgreSQL.
     * @param publisher  Publicador Kafka.
     * @param objectMapper Serializador JSON.
     */
    public IngestionService(final PlayerEventRepository repository,
                            final EventPublisher publisher,
                            final ObjectMapper objectMapper) {
        this.repository = repository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Persiste o evento no banco (Event Store) e emite para o Kafka.
     *
     * @param rawEvent Evento de entrada.
     * @return Evento enriquecido com UUID e Timestamp.
     */
    @Transactional
    public GameEvent ingestAndPublish(final GameEvent rawEvent) {
        GameEvent event = rawEvent.withGeneratedMetadata();

        String payloadJson = null;
        if (event.payload() != null && !event.payload().isEmpty()) {
            try {
                payloadJson = objectMapper.writeValueAsString(event.payload());
            } catch (JsonProcessingException e) {
                payloadJson = "{}";
            }
        }

        PlayerEventEntity entity = new PlayerEventEntity(event, payloadJson);

        repository.save(entity);
        publisher.publish(event);

        return event;
    }
}
