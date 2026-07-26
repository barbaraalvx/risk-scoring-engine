package com.antifraude.ingestion;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.antifraude.ingestion.model.GameEvent;
import com.antifraude.ingestion.model.GameEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;
    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        eventPublisher = new EventPublisher(kafkaTemplate, objectMapper, "player-actions");
    }

    @Test
    void shouldPublishEventToKafkaUsingPlayerIdAsKey() throws Exception {
        UUID eventId = UUID.randomUUID();
        GameEvent event = new GameEvent(
                eventId,
                "player-999",
                GameEventType.BET,
                Instant.parse("2026-07-20T12:00:00Z"),
                "sess-123",
                "fp-xyz",
                "10.0.0.1",
                Map.of("amount", 100.0)
        );

        eventPublisher.publish(event);

        String expectedPayload = objectMapper.writeValueAsString(event);

        verify(kafkaTemplate).send(eq("player-actions"), eq("player-999"), eq(expectedPayload));
    }
}
