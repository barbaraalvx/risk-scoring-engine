package com.antifraude.quarantine.readmodel;

import java.time.Instant;

import com.antifraude.quarantine.domain.QuarantineStatus;

/**
 * Representação do read model (CQRS) de uma quarentena, mantida em Redis
 * para consulta rápida pelo painel administrativo, sem tocar o Postgres.
 *
 * @param playerId  Identificador do jogador.
 * @param status    Estado atual da quarentena.
 * @param reason    Motivo do estado atual.
 * @param totalScore Score total que originou a quarentena.
 * @param updatedAt Momento da última atualização da projeção.
 */
public record QuarantineView(

        String playerId,

        QuarantineStatus status,

        String reason,

        int totalScore,

        Instant updatedAt

) {
}
