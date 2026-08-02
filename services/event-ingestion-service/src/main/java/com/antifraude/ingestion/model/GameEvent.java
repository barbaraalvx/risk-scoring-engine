package com.antifraude.ingestion.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Representação do evento de ação de jogador.
 *
 * @param eventId           Identificador único do evento.
 * @param playerId          Identificador do jogador.
 * @param eventType         Tipo do evento.
 * @param timestamp         Timestamp ISO-8601 da ocorrência.
 * @param sessionId         ID da sessão do jogador.
 * @param deviceFingerprint Fingerprint do dispositivo.
 * @param ipAddress         Endereço IP de origem.
 * @param payload           Dados específicos da ação.
 */
public record GameEvent(
        UUID eventId,
        @NotBlank(message = "playerId é obrigatório") String playerId,
        @NotNull(message = "eventType é obrigatório") GameEventType eventType,
        Instant timestamp,
        String sessionId,
        String deviceFingerprint,
        String ipAddress,
        Map<String, Object> payload) {

    /**
     * Construtor compacto para validação e inicialização de defaults.
     */
    public GameEvent {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        payload = normalizePayload(payload);
    }

    /**
     * Retorna uma nova instância garantindo que eventId e timestamp estejam preenchidos.
     *
     * @return Novo GameEvent enriquecido.
     */
    public GameEvent withGeneratedMetadata() {
        UUID newEventId = (eventId != null) ? eventId : UUID.randomUUID();
        Instant newTimestamp = (timestamp != null) ? timestamp : Instant.now();
        return new GameEvent(
                newEventId, playerId, eventType, newTimestamp,
                sessionId, deviceFingerprint, ipAddress, payload);
    }

    private static Map<String, Object> normalizePayload(final Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }
}
