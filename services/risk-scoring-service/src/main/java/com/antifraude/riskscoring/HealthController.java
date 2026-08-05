package com.antifraude.riskscoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a health-check endpoint for the Risk Scoring Service.
 */
@RestController
public class HealthController {

    /**
     * Returns the health status of the service.
     *
     * @return a plain-text message indicating the service is up
     */
    @GetMapping("/health")
    public String health() {
        return "risk-scoring-service OK";
    }
}
