package com.antifraude.quarantine.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado pelo Risk Scoring Service após calcular
 * o score de risco de um jogador.
 */
public record ScoreUpdatedEvent(
        UUID eventId,
        String playerId,
        int totalScore,
        double quarantineThreshold,
        Instant calculatedAt

) {
}