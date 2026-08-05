package com.antifraude.riskscoring.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA que representa um registro imutável de score de risco calculado.
 */
@Entity
@Table(name = "risk_scores")
public class PlayerScoreRecord {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private String playerId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "device_fingerprint_score", nullable = false)
    private int deviceFingerprintScore;

    @Column(name = "action_velocity_score", nullable = false)
    private int actionVelocityScore;

    @Column(name = "choice_pattern_score", nullable = false)
    private int choicePatternScore;

    @Column(name = "multi_account_score", nullable = false)
    private int multiAccountScore;

    @Column(name = "quarantine_triggered", nullable = false)
    private boolean quarantineTriggered;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    /**
     * Construtor padrão JPA.
     */
    public PlayerScoreRecord() {
    }

    /**
     * Construtor completo.
     *
     * @param id                     ID único do registro.
     * @param playerId              ID do jogador.
     * @param eventId               ID do evento gerador.
     * @param eventType             Tipo do evento.
     * @param totalScore            Score total (0-100).
     * @param deviceFingerprintScore Sub-score de device.
     * @param actionVelocityScore    Sub-score de velocidade.
     * @param choicePatternScore     Sub-score de padrões.
     * @param multiAccountScore      Sub-score multi-conta.
     * @param quarantineTriggered   Flag de quarentena.
     * @param calculatedAt          Data/hora do cálculo.
     */
    public PlayerScoreRecord(final UUID id, final String playerId, final UUID eventId,
                             final String eventType, final int totalScore,
                             final int deviceFingerprintScore, final int actionVelocityScore,
                             final int choicePatternScore, final int multiAccountScore,
                             final boolean quarantineTriggered, final Instant calculatedAt) {
        this.id = id;
        this.playerId = playerId;
        this.eventId = eventId;
        this.eventType = eventType;
        this.totalScore = totalScore;
        this.deviceFingerprintScore = deviceFingerprintScore;
        this.actionVelocityScore = actionVelocityScore;
        this.choicePatternScore = choicePatternScore;
        this.multiAccountScore = multiAccountScore;
        this.quarantineTriggered = quarantineTriggered;
        this.calculatedAt = calculatedAt;
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

    public String getEventType() {
        return eventType;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getDeviceFingerprintScore() {
        return deviceFingerprintScore;
    }

    public int getActionVelocityScore() {
        return actionVelocityScore;
    }

    public int getChoicePatternScore() {
        return choicePatternScore;
    }

    public int getMultiAccountScore() {
        return multiAccountScore;
    }

    public boolean isQuarantineTriggered() {
        return quarantineTriggered;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
