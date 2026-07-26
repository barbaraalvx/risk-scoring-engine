package com.antifraude.riskscoring.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.antifraude.riskscoring.domain.PlayerScoreRecord;
import com.antifraude.riskscoring.model.GameEvent;
import com.antifraude.riskscoring.service.rules.ActionVelocityRule;
import com.antifraude.riskscoring.service.rules.ChoicePatternRule;
import com.antifraude.riskscoring.service.rules.DeviceFingerprintRule;
import com.antifraude.riskscoring.service.rules.MultiAccountRule;

/**
 * Motor de Scoring Multifatorial.
 * Agrega os sub-scores das 4 regras e aplica os pesos dinâmicos (Feature Flags).
 */
@Service
public class ScoringEngine {

    private final DeviceFingerprintRule deviceRule;
    private final ActionVelocityRule velocityRule;
    private final ChoicePatternRule patternRule;
    private final MultiAccountRule multiAccountRule;

    /**
     * Construtor injetando as 4 regras de risco.
     *
     * @param deviceRule       Regra de fingerprint.
     * @param velocityRule     Regra de velocidade.
     * @param patternRule      Regra de padrões de escolha.
     * @param multiAccountRule Regra de multi-contas.
     */
    public ScoringEngine(final DeviceFingerprintRule deviceRule,
                         final ActionVelocityRule velocityRule,
                         final ChoicePatternRule patternRule,
                         final MultiAccountRule multiAccountRule) {
        this.deviceRule = deviceRule;
        this.velocityRule = velocityRule;
        this.patternRule = patternRule;
        this.multiAccountRule = multiAccountRule;
    }

    /**
     * Calcula o score multifatorial e gera o registro imutável PlayerScoreRecord.
     *
     * @param event   Evento do jogador.
     * @param weights Pesos ativos das Feature Flags.
     * @return PlayerScoreRecord populado.
     */
    public PlayerScoreRecord calculate(final GameEvent event, final ScoringWeights weights) {
        int deviceScore = deviceRule.calculate(event);
        int velocityScore = velocityRule.calculate(event);
        int patternScore = patternRule.calculate(event);
        int multiAccountScore = multiAccountRule.calculate(event);

        // Soma ponderada (cada sub-score vai de 0 a 25)
        double weightedSum = (deviceScore * weights.deviceWeight())
                + (velocityScore * weights.velocityWeight())
                + (patternScore * weights.patternWeight())
                + (multiAccountScore * weights.multiAccountWeight());

        // Escala normalizada para (0 - 100)
        int totalScore = (int) Math.min(100, Math.max(0, Math.round(weightedSum * 4.0)));

        boolean quarantineTriggered = totalScore >= weights.quarantineThreshold();

        UUID eventIdUUID = null;
        if (event.eventId() != null && !event.eventId().isBlank()) {
            try {
                eventIdUUID = UUID.fromString(event.eventId());
            } catch (IllegalArgumentException ignored) {
                eventIdUUID = UUID.randomUUID();
            }
        } else {
            eventIdUUID = UUID.randomUUID();
        }

        return new PlayerScoreRecord(
                UUID.randomUUID(),
                event.playerId(),
                eventIdUUID,
                event.eventType() != null ? event.eventType() : "UNKNOWN",
                totalScore,
                deviceScore,
                velocityScore,
                patternScore,
                multiAccountScore,
                quarantineTriggered,
                Instant.now()
        );
    }
}
