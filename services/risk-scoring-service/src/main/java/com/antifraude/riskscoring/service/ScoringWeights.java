package com.antifraude.riskscoring.service;

/**
 * Value Object imutável contendo os pesos das regras de scoring e limiar de quarentena (Feature Flags).
 *
 * <p>As flags booleanas usam {@link Boolean} (e não {@code boolean}) de propósito: registros salvos no
 * Redis antes da introdução de um novo campo não terão essa chave no JSON, e o construtor compacto
 * abaixo trata a ausência ({@code null}) como fail-safe habilitado, em vez de desabilitar a regra
 * silenciosamente após o deploy.</p>
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
        Boolean deviceRuleEnabled,
        Boolean velocityRuleEnabled,
        Boolean patternRuleEnabled,
        Boolean multiAccountRuleEnabled,
        Boolean quarantineEnabled,
        Boolean adminMonitoringEnabled) {

    /**
     * Construtor compacto que aplica o fallback fail-safe (habilitado) às flags booleanas ausentes,
     * cobrindo o caso de JSON legado no Redis sem os campos mais recentes.
     */
    public ScoringWeights {
        deviceRuleEnabled = deviceRuleEnabled == null ? true : deviceRuleEnabled;
        velocityRuleEnabled = velocityRuleEnabled == null ? true : velocityRuleEnabled;
        patternRuleEnabled = patternRuleEnabled == null ? true : patternRuleEnabled;
        multiAccountRuleEnabled = multiAccountRuleEnabled == null ? true : multiAccountRuleEnabled;
        quarantineEnabled = quarantineEnabled == null ? true : quarantineEnabled;
        adminMonitoringEnabled = adminMonitoringEnabled == null ? true : adminMonitoringEnabled;
    }

    /**
     * Retorna os pesos padrão fail-safe.
     *
     * @return Instância padrão de ScoringWeights.
     */
    public static ScoringWeights defaultConfig() {
        return new ScoringWeights(0.25, 0.25, 0.25, 0.25, 70.0, true, true, true, true, true, true);
    }
}
