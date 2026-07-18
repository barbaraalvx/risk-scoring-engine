package com.antifraude.quarantine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Quarantine Service application.
 */
@SpringBootApplication
public class Application {

    // Use default constructor pattern for Spring Boot application entry

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

