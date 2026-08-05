package com.antifraude.riskscoring.service.rules;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.antifraude.riskscoring.model.GameEvent;

/**
 * Regra 1: Device Fingerprint Rule.
 * Pontua se o evento contiver um fingerprint suspeito ou atípico.
 */
@Component
public class DeviceFingerprintRule implements RiskRule {

    @Override
    public int calculate(final GameEvent event) {
        if (event == null || event.deviceFingerprint() == null || event.deviceFingerprint().isBlank()) {
            // Ausência de device fingerprint é sinal suspeito (sub-score alto)
            return 20;
        }

        String fp = event.deviceFingerprint().toLowerCase(Locale.ROOT);
        if (fp.contains("emulator") || fp.contains("unknown") || fp.contains("root")) {
            return 25;
        }

        return 0;
    }
}
