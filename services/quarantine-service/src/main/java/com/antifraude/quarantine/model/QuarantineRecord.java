package com.antifraude.quarantine.model;

import java.time.Instant;
import java.util.UUID;

import com.antifraude.quarantine.domain.QuarantineStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA que representa um registro de quarentena de um jogador.
 */
@Entity
@Table(
    name = "quarantine_records"
)
public class QuarantineRecord {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private String playerId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuarantineStatus status;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Construtor padrão exigido pelo JPA.
     */
    protected QuarantineRecord() {
    }

    /**
     * Construtor completo.
     *
     * @param id ID único da quarentena.
     * @param playerId Identificador do jogador.
     * @param eventId Evento que originou a quarentena.
     * @param totalScore Score total que disparou a quarentena.
     * @param status Estado atual da quarentena.
     * @param reason Motivo resumido da quarentena.
     * @param createdAt Data de criação.
     * @param resolvedAt Data de resolução (caso exista).
     */
    public QuarantineRecord(
            final UUID id,
            final String playerId,
            final UUID eventId,
            final int totalScore,
            final QuarantineStatus status,
            final String reason,
            final Instant createdAt,
            final Instant resolvedAt) {

        this.id = id;
        this.playerId = playerId;
        this.eventId = eventId;
        this.totalScore = totalScore;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public QuarantineStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}