package com.antifraude.quarantine.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado pelo Risk Scoring Service após calcular
 * o score de risco de um jogador.
 *
 * @param eventId Identificador único do evento.
 * @param playerId Identificador do jogador avaliado.
 * @param totalScore Score total calculado para o jogador.
 * @param quarantineThreshold Limite a partir do qual a quarentena é acionada.
 * @param calculatedAt Momento em que o score foi calculado.
 */
public record ScoreUpdatedEvent(
        UUID eventId,
        String playerId,
        int totalScore,
        double quarantineThreshold,
        Instant calculatedAt

) {
}
