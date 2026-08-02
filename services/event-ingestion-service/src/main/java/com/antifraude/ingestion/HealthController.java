package com.antifraude.ingestion;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de verificação de saúde do serviço de Ingestão de Eventos.
 */
@RestController
public class HealthController {

    /**
     * Endpoint simples de verificação de saúde.
     *
     * @return Status OK do serviço.
     */
    @GetMapping("/health")
    public String health() {
        return "event-ingestion-service OK";
    }
}
