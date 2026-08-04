package com.antifraude.riskscoring.service;

import java.nio.charset.StandardCharsets;
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
        int deviceScore = weights.deviceRuleEnabled() ? deviceRule.calculate(event) : 0;
        int velocityScore = weights.velocityRuleEnabled() ? velocityRule.calculate(event) : 0;
        int patternScore = weights.patternRuleEnabled() ? patternRule.calculate(event) : 0;
        int multiAccountScore = weights.multiAccountRuleEnabled() ? multiAccountRule.calculate(event) : 0;

        // Soma ponderada (cada sub-score vai de 0 a 25)
        double weightedSum = (deviceScore * weights.deviceWeight())
                + (velocityScore * weights.velocityWeight())
                + (patternScore * weights.patternWeight())
                + (multiAccountScore * weights.multiAccountWeight());

        // Escala normalizada para (0 - 100)
        int totalScore = Math.clamp(Math.round(weightedSum * 4.0), 0, 100);

        boolean quarantineTriggered = weights.quarantineEnabled()
                && totalScore >= weights.quarantineThreshold();

        UUID eventIdUUID;
        if (event.eventId() != null && !event.eventId().isBlank()) {
            try {
                eventIdUUID = UUID.fromString(event.eventId());
            } catch (IllegalArgumentException ignored) {
                // Deriva um UUID determinístico do eventId original para preservar
                // idempotência: o mesmo evento reprocessado deve gerar o mesmo UUID.
                eventIdUUID = UUID.nameUUIDFromBytes(event.eventId().getBytes(StandardCharsets.UTF_8));
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
