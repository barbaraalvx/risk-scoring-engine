package com.antifraude.riskscoring.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento emitido quando o score de risco é atualizado.
 *
 * @param eventId ID do evento que originou o cálculo.
 * @param playerId ID do jogador avaliado.
 * @param totalScore Score total calculado.
 * @param quarantineThreshold Limite a partir do qual a quarentena é acionada.
 * @param calculatedAt Momento em que o score foi calculado.
 */
public record ScoreUpdatedEvent(
        UUID eventId,
        String playerId,
        int totalScore,
        double quarantineThreshold,
        Instant calculatedAt) {
}
