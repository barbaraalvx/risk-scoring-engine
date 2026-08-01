package com.antifraude.quarantine.event;

import java.time.Instant;
import java.util.UUID;

import com.antifraude.quarantine.domain.QuarantineStatus;

/**
 * Evento publicado quando o estado de uma quarentena é atualizado.
 *
 * Esse evento será consumido futuramente pelo painel administrativo.
 *
 * @param playerId   Jogador afetado.
 * @param eventId    Evento que originou a quarentena.
 * @param status     Estado atual da quarentena.
 * @param reason     Motivo da quarentena.
 * @param resolvedAt Momento em que a quarentena foi concluída.
 */
public record QuarantineUpdatedEvent(

        String playerId,

        UUID eventId,
        
        QuarantineStatus status,

        String reason,

        Instant timestamp

) {
}