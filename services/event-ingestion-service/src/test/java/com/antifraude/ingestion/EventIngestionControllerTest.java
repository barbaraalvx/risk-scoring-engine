package com.antifraude.ingestion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import com.antifraude.ingestion.model.GameEvent;
import com.antifraude.ingestion.model.GameEventType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EventIngestionController.class)
@Import(ApiExceptionHandler.class)
class EventIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventPublisher eventPublisher;

    @Test
    void shouldAcceptEventAndReturnGeneratedEventId() throws Exception {
        GameEvent eventWithoutId = new GameEvent(
                null,
                "player-123",
                GameEventType.BET,
                Instant.parse("2026-07-18T12:30:00Z"),
                Map.of("amount", 200.50));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventWithoutId)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").isNotEmpty());

        verify(eventPublisher).publish(any(GameEvent.class));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String invalidPayload = """
                {
                  "playerId": "",
                  "eventType": "BET",
                  "payload": {
                    "amount": 50
                  }
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Payload invalido."))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[0].field").isNotEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        String malformedJson = "{\"playerId\": \"player-1\", \"eventType\": BET}";

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Payload invalido."));
    }
}
