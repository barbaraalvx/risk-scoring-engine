package com.antifraude.riskscoring.service.rules;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.antifraude.riskscoring.model.GameEvent;

/**
 * Regra 3: Choice Pattern Rule.
 * Pontua com base em desvios no padrão de apostas e valores atípicos.
 */
@Component
public class ChoicePatternRule implements RiskRule {

    @Override
    public int calculate(final GameEvent event) {
        if (event == null || event.payload() == null) {
            return 0;
        }

        Map<String, Object> payload = event.payload();
        if (payload.containsKey("amount")) {
            try {
                double amount = Double.parseDouble(payload.get("amount").toString());
                if (amount >= 5000.0) {
                    return 25; // Aposta muito alta / desvio extremo
                } else if (amount >= 2000.0) {
                    return 15;
                }
            } catch (NumberFormatException ignored) {
                return 5;
            }
        }

        return 0;
    }
}
