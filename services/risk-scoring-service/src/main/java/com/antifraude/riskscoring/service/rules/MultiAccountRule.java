package com.antifraude.riskscoring.service.rules;

import org.springframework.stereotype.Component;

import com.antifraude.riskscoring.model.GameEvent;

/**
 * Regra 4: Multi-Account Correlation Rule.
 * Pontua quando o evento envolve tipos explícitos de vinculação de conta ou suspeita de multi-conta.
 */
@Component
public class MultiAccountRule implements RiskRule {

    @Override
    public int calculate(final GameEvent event) {
        if (event == null || event.eventType() == null) {
            return 0;
        }

        String type = event.eventType().toUpperCase();
        if ("MULTI_ACCOUNT_SUSPECT".equals(type) || "ACCOUNT_LINK".equals(type)) {
            return 25;
        }

        // IP de rede privada/suspeita compartilhado
        if (event.ipAddress() != null && event.ipAddress().startsWith("10.0.0.")) {
            return 10;
        }

        return 0;
    }
}
