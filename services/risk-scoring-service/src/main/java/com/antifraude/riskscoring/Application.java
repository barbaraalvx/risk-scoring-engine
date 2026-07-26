package com.antifraude.riskscoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Risk Scoring Service application.
 */
@SpringBootApplication
public class Application {

    /** Utility class — do not instantiate. */
    Application() {
    }

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
