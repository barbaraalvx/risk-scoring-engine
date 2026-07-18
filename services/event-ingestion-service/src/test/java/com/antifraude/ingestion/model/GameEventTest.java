package com.antifraude.ingestion.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class GameEventTest {

    // Valida as anotações Bean Validation aplicadas no record.
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldNormalizePayloadAndProtectInternalState() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", 150.75);

        GameEvent gameEvent = new GameEvent(
                null,
                "player-123",
                GameEventType.BET,
                Instant.parse("2026-07-18T12:30:00Z"),
                payload);

        assertEquals(1, gameEvent.payload().size());
        assertEquals(150.75, gameEvent.payload().get("amount"));
        // O payload interno não deve aceitar mutação direta.
        assertThrows(UnsupportedOperationException.class, () -> gameEvent.payload().put("currency", "BRL"));

        payload.put("currency", "BRL");

        assertFalse(gameEvent.payload().containsKey("currency"));
    }

    @Test
    void shouldGenerateEventIdWhenMissing() {
        GameEvent gameEvent = new GameEvent(
                null,
                "player-123",
                GameEventType.LOGIN,
                Instant.parse("2026-07-18T12:30:00Z"),
                Map.of());

        GameEvent normalized = gameEvent.withGeneratedEventId();

        assertNotNull(normalized.eventId());
    }

    @Test
    void shouldKeepExistingEventIdWhenPresent() {
        UUID eventId = UUID.randomUUID();

        GameEvent gameEvent = new GameEvent(
                eventId,
                "player-123",
                GameEventType.DEPOSIT,
                Instant.parse("2026-07-18T12:30:00Z"),
                Map.of());

        GameEvent normalized = gameEvent.withGeneratedEventId();

        assertEquals(eventId, normalized.eventId());
    }

    @Test
    void shouldEnforceBeanValidationOnRequiredFields() {
        // Campos obrigatórios vazios ou nulos devem gerar violações.
        GameEvent gameEvent = new GameEvent(
                null,
                "",
                null,
                null,
                Map.of());

        assertFalse(validator.validate(gameEvent).isEmpty());
    }
}