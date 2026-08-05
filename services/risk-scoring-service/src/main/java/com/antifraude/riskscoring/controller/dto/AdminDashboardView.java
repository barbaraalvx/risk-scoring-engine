package com.antifraude.riskscoring.controller.dto;

import java.util.List;

import com.antifraude.riskscoring.service.ScoringWeights;

/**
 * DTO de resposta para o painel administrativo.
 *
 * @param flags Pesos e flags ativas do motor.
 * @param totalScores Total de scores calculados.
 * @param quarantinedScores Total de registros em quarentena.
 * @param recentSignals Sinais de risco recentes.
 * @param quarantineHistory Histórico de quarentenas por jogador.
 */
public record AdminDashboardView(
        ScoringWeights flags,
        long totalScores,
        long quarantinedScores,
        List<RecentScoreSignalView> recentSignals,
        List<QuarantineHistoryView> quarantineHistory) {
}
