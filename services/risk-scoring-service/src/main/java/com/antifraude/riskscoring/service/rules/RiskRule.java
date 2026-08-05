package com.antifraude.riskscoring.service.rules;

import com.antifraude.riskscoring.model.GameEvent;

/**
 * Interface que define o contrato das regras de cálculo de score de risco.
 */
public interface RiskRule {

    /**
     * Calcula o sub-score parcial para o evento fornecido (de 0 a 25 pontos).
     *
     * @param event Evento do jogador.
     * @return Sub-score normalizado (0 - 25).
     */
    int calculate(GameEvent event);
}
