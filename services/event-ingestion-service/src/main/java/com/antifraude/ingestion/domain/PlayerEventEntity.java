package com.antifraude.ingestion.domain;

import java.time.Instant;
import java.util.UUID;

import com.antifraude.ingestion.model.GameEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA representando um evento no Event Store (PostgreSQL).
 */
@Entity
@Table(name = "player_events")
public class PlayerEventEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private String playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private GameEventType eventType;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    /**
     * Construtor padrão JPA.
     */
    public PlayerEventEntity() {
    }

    /**
     * Construtor completo.
     *
     * @param id                Identificador único.
     * @param playerId         ID do jogador.
     * @param eventType        Tipo do evento.
     * @param timestamp        Data/hora.
     * @param sessionId        ID da sessão.
     * @param deviceFingerprint Fingerprint.
     * @param ipAddress        Endereço IP.
     * @param payload          Payload em JSON.
     */
    public PlayerEventEntity(final UUID id, final String playerId, final GameEventType eventType,
                             final Instant timestamp, final String sessionId,
                             final String deviceFingerprint, final String ipAddress,
                             final String payload) {
        this.id = id;
        this.playerId = playerId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
        this.deviceFingerprint = deviceFingerprint;
        this.ipAddress = ipAddress;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public GameEventType getEventType() {
        return eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPayload() {
        return payload;
    }
}
