package com.antifraude.riskscoring.controller.dto;

import java.time.Instant;

/**
 * DTO de um sinal recente exibido no painel admin.
 */
public record RecentScoreSignalView(
        String playerId,
        int totalScore,
        boolean quarantineTriggered,
        Instant calculatedAt) {
}
