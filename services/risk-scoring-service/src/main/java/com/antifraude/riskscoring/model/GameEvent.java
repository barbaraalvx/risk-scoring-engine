package com.antifraude.riskscoring.model;

import java.util.Map;

/**
 * DTO contendo o evento bruto recebido do Ingestor.
 *
 * @param eventId ID único do evento.
 * @param playerId ID do jogador.
 * @param eventType Tipo do evento.
 * @param timestamp Momento em que o evento ocorreu.
 * @param sessionId ID da sessão do jogador.
 * @param deviceFingerprint Fingerprint do dispositivo utilizado.
 * @param ipAddress Endereço IP de origem.
 * @param payload Dados adicionais do evento.
 */
public record GameEvent(
        String eventId,
        String playerId,
        String eventType,
        String timestamp,
        String sessionId,
        String deviceFingerprint,
        String ipAddress,
        Map<String, Object> payload) {
}
