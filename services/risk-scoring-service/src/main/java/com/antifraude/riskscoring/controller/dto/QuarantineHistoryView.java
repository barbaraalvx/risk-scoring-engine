package com.antifraude.riskscoring.controller.dto;

import java.util.List;

/**
 * DTO resumido com status de quarentena e histórico para o dashboard admin.
 *
 * @param playerId ID do jogador.
 * @param status Status atual da quarentena.
 * @param reason Motivo da quarentena.
 * @param history Lista de eventos históricos de quarentena.
 */
public record QuarantineHistoryView(
        String playerId,
        String status,
        String reason,
        List<QuarantineEventView> history) {
}
