package com.antifraude.riskscoring.controller.dto;

import java.time.Instant;

/**
 * DTO de um evento de quarentena exibido no histórico do painel admin.
 *
 * @param status Status da transição de quarentena.
 * @param reason Motivo do isolamento.
 * @param createdAt Data e hora de criação do evento.
 */
public record QuarantineEventView(
        String status,
        String reason,
        Instant createdAt) {
}
