package com.antifraude.riskscoring.model;

import java.util.Map;

/**
 * DTO contendo o evento bruto recebido do Ingestor.
 */
public record GameEvent(
        String eventId,
        String playerId,
        String eventType,
        String timestamp,
        String sessionId,
        String deviceFingerprint,
        String ipAddress,
        Map<String, Object> payload) {
}
