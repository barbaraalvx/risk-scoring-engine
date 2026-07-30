package com.antifraude.quarantine.event;

import java.time.Instant;
import java.util.UUID;

import com.antifraude.quarantine.domain.QuarantineStatus;

/**
 * Evento publicado após alteração do estado de uma quarentena.
 */
public record QuarantineUpdatedEvent(

        UUID quarantineId,

        String playerId,

        QuarantineStatus status,

        String reason,

        Instant timestamp

) {
}