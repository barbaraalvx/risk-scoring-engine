package com.antifraude.quarantine.domain;

/**
 * Estados possíveis de uma quarentena.
 */
public enum QuarantineStatus {

    /**
     * Quarentena criada, aguardando conclusão do fluxo.
     */
    PENDING,

    /**
     * Jogador colocado em quarentena com sucesso.
     */
    QUARANTINED,

    /**
     * O fluxo falhou durante a execução.
     */
    FAILED,

    /**
     * O fluxo foi compensado (rollback).
     */
    COMPENSATED
}