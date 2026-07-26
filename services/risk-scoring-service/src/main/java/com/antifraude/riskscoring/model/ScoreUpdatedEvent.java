package com.antifraude.riskscoring.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido quando o score de risco é atualizado.
 */
public record ScoreUpdatedEvent(
        UUID eventId,
        String playerId,
        int totalScore,
        double quarantineThreshold,
        Instant calculatedAt) {
}
