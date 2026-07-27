package com.antifraude.ingestion.model;

/**
 * Tipos de eventos suportados pelo sistema de antifraude.
 */
public enum GameEventType {
    /** Evento de login do jogador. */
    LOGIN,
    /** Evento de movimentação no jogo. */
    MOVE,
    /** Evento de aposta no jogo. */
    BET,
    /** Evento de vinculação entre contas. */
    ACCOUNT_LINK,
    /** Evento de captura de fingerprint de dispositivo. */
    DEVICE_FINGERPRINT,
    /** Evento de solicitação de saque. */
    WITHDRAWAL,
    /** Evento de depósito de fundos. */
    DEPOSIT
}