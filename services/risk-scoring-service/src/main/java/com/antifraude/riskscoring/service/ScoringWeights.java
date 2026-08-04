package com.antifraude.riskscoring.service;

/**
 * Value Object imutável contendo os pesos das regras de scoring e limiar de quarentena (Feature Flags).
 *
 * @param deviceWeight            Peso da regra de Device Fingerprint (default 0.25).
 * @param velocityWeight          Peso da regra de Velocidade de Ações (default 0.25).
 * @param patternWeight           Peso da regra de Padrão de Escolhas (default 0.25).
 * @param multiAccountWeight      Peso da regra de Multi-Contas / Correlação (default 0.25).
 * @param quarantineThreshold     Limiar para acionar a Quarentena (default 70.0).
 * @param deviceRuleEnabled       Habilita a regra de fingerprint.
 * @param velocityRuleEnabled     Habilita a regra de velocidade.
 * @param patternRuleEnabled      Habilita a regra de padrão de escolhas.
 * @param multiAccountRuleEnabled Habilita a regra de multi-conta.
 * @param quarantineEnabled       Habilita a ação de quarentena automática.
 * @param adminMonitoringEnabled  Habilita a coleta de métricas no painel admin.
 */
public record ScoringWeights(
        double deviceWeight,
        double velocityWeight,
        double patternWeight,
        double multiAccountWeight,
        double quarantineThreshold,
        boolean deviceRuleEnabled,
        boolean velocityRuleEnabled,
        boolean patternRuleEnabled,
        boolean multiAccountRuleEnabled,
        boolean quarantineEnabled,
        boolean adminMonitoringEnabled) {

    /**
     * Retorna os pesos padrão fail-safe.
     *
     * @return Instância padrão de ScoringWeights.
     */
    public static ScoringWeights defaultConfig() {
        return new ScoringWeights(0.25, 0.25, 0.25, 0.25, 70.0, true, true, true, true, true, true);
    }
}
