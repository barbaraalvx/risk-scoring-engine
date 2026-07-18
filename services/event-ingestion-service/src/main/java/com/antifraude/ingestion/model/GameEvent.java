package com.antifraude.ingestion.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GameEvent(
        UUID eventId,
        @NotBlank String playerId,
        @NotNull GameEventType eventType,
        @NotNull Instant timestamp,
        Map<String, Object> payload) {

    public GameEvent {
        // Garante um payload estável e imutável dentro do modelo.
        payload = normalizePayload(payload);
    }

    public GameEvent withGeneratedEventId() {
        if (eventId != null) {
            return this;
        }

        // O servidor gera o identificador quando ele não vier na requisição.
        return new GameEvent(UUID.randomUUID(), playerId, eventType, timestamp, payload);
    }

    private static Map<String, Object> normalizePayload(final Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            // Evita nulo e simplifica o uso em etapas seguintes do fluxo.
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }
}