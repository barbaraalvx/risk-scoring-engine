package com.antifraude.riskscoring.controller.dto;

import java.util.List;

/**
 * DTO resumido com status de quarentena e histórico para o dashboard admin.
 */
public record QuarantineHistoryView(
        String playerId,
        String status,
        String reason,
        List<QuarantineEventView> history) {
}
