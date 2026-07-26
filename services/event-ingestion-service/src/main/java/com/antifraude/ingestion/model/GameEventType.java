package com.antifraude.ingestion.model;

/**
 * Tipos de eventos suportados pelo sistema de antifraude.
 */
public enum GameEventType {
    LOGIN,
    MOVE,
    BET,
    ACCOUNT_LINK,
    DEVICE_FINGERPRINT,
    WITHDRAWAL,
    DEPOSIT,
    MULTI_ACCOUNT_SUSPECT
}