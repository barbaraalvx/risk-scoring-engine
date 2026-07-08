package com.antifraude.riskscoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public final class Application {

    private Application() {
    }

    /**
     * Starts the Risk Scoring Spring Boot application.
     *
     * @param args startup arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(RiskScoringServiceConfiguration.class, args);
    }
}

@SpringBootApplication
class RiskScoringServiceConfiguration {
}

@RestController
class HealthController {

    @GetMapping("/health")
    public String health() {
        return "risk-scoring-service OK";
    }
}
