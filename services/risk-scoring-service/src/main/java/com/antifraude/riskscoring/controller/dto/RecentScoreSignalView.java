package com.antifraude.riskscoring.controller.dto;

import java.time.Instant;

/**
 * DTO de um sinal recente exibido no painel admin.
 *
 * @param playerId ID do jogador.
 * @param totalScore Pontuação de risco calculada.
 * @param quarantineTriggered Indica se a quarentena foi acionada.
 * @param calculatedAt Data e hora do cálculo.
 */
public record RecentScoreSignalView(
        String playerId,
        int totalScore,
        boolean quarantineTriggered,
        Instant calculatedAt) {
}
