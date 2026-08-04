package com.antifraude.riskscoring.controller.dto;

import java.time.Instant;

/**
 * DTO de um evento de quarentena exibido no histórico do painel admin.
 */
public record QuarantineEventView(
        String status,
        String reason,
        Instant createdAt) {
}
