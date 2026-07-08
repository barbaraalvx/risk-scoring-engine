package com.antifraude.quarantine;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a health-check endpoint for the Quarantine Service.
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
        return "quarantine-service OK";
    }
}
