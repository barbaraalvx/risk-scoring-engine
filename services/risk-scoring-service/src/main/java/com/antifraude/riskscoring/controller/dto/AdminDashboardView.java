package com.antifraude.riskscoring.controller.dto;

import java.util.List;

import com.antifraude.riskscoring.service.ScoringWeights;

/**
 * DTO de resposta para o painel administrativo.
 */
public record AdminDashboardView(
        ScoringWeights flags,
        long totalScores,
        long quarantinedScores,
        List<RecentScoreSignalView> recentSignals,
        List<QuarantineHistoryView> quarantineHistory) {
}
