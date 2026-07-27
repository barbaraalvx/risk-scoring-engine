package com.antifraude.ingestion.domain;

import java.time.Instant;
import java.util.UUID;

import com.antifraude.ingestion.model.GameEvent;
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
     * Construtor a partir do modelo GameEvent.
     *
     * @param event   Evento de jogo.
     * @param payload JSON do payload.
     */
    public PlayerEventEntity(final GameEvent event, final String payload) {
        this.id = event.eventId();
        this.playerId = event.playerId();
        this.eventType = event.eventType();
        this.timestamp = event.timestamp();
        this.sessionId = event.sessionId();
        this.deviceFingerprint = event.deviceFingerprint();
        this.ipAddress = event.ipAddress();
        this.payload = payload;
    }

    /**
     * Identificador do evento.
     *
     * @return UUID.
     */
    public UUID getId() {
        return id;
    }

    /**
     * ID do jogador.
     *
     * @return String.
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Tipo do evento.
     *
     * @return GameEventType.
     */
    public GameEventType getEventType() {
        return eventType;
    }

    /**
     * Timestamp do evento.
     *
     * @return Instant.
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * ID da sessão.
     *
     * @return String.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Fingerprint do dispositivo.
     *
     * @return String.
     */
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    /**
     * Endereço IP.
     *
     * @return String.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Payload JSON.
     *
     * @return String.
     */
    public String getPayload() {
        return payload;
    }
}
