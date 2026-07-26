package com.antifraude.riskscoring.service.rules;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.antifraude.riskscoring.model.GameEvent;
import com.antifraude.riskscoring.repository.PlayerScoreRepository;

/**
 * Regra 2: Action Velocity Rule.
 * Pontua se o jogador realizou múltiplos eventos em um curto intervalo de tempo (comportamento de Bot).
 */
@Component
public class ActionVelocityRule implements RiskRule {

    private final PlayerScoreRepository repository;

    /**
     * Construtor injetando repositório.
     *
     * @param repository Repositório de scores.
     */
    public ActionVelocityRule(final PlayerScoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public int calculate(final GameEvent event) {
        if (event == null || event.playerId() == null) {
            return 0;
        }

        // Janela de 10 segundos
        Instant tenSecondsAgo = Instant.now().minus(Duration.ofSeconds(10));
        long recentActions = repository.countActionsSince(event.playerId(), tenSecondsAgo);

        if (recentActions >= 5) {
            return 25; // Bot suspeito
        } else if (recentActions >= 3) {
            return 15;
        }

        return 0;
    }
}
