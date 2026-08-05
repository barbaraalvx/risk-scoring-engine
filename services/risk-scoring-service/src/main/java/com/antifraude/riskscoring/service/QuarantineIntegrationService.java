package com.antifraude.riskscoring.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.antifraude.riskscoring.controller.dto.QuarantineHistoryView;

/**
 * Serviço de integração com o microserviço de quarentena para o painel administrativo.
 */
@Service
public class QuarantineIntegrationService {

    private final WebClient webClient;

    /**
     * Construtor do serviço de integração com quarentena.
     *
     * @param webClientBuilder  Builder do WebClient reativo.
     * @param quarantineBaseUrl URL base do serviço de quarentena.
     */
    public QuarantineIntegrationService(
            final WebClient.Builder webClientBuilder,
            @Value("${quarantine.base-url:http://quarantine-service:8082}") final String quarantineBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(quarantineBaseUrl == null ? "http://quarantine-service:8082" : quarantineBaseUrl).build();
    }

    /**
     * Busca o estado atual dos jogadores em quarentena para exibição no painel.
     *
     * @return Lista de status resumidos por jogador.
     */
    public List<QuarantineHistoryView> getLatestQuarantineStatus() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> payload = webClient.get()
                .uri("/quarantine")
                .retrieve()
                .bodyToMono(List.class)
                .onErrorReturn(List.of())
                .block();

        if (payload == null || payload.isEmpty()) {
            return List.of();
        }

        List<QuarantineHistoryView> result = new ArrayList<>();
        for (Map<String, Object> entry : payload) {
            String playerId = String.valueOf(entry.getOrDefault("playerId", "unknown"));
            String status = String.valueOf(entry.getOrDefault("status", "UNKNOWN"));
            String reason = String.valueOf(entry.getOrDefault("reason", "Sem motivo informado"));
            result.add(new QuarantineHistoryView(playerId, status, reason, List.of()));
        }
        return result;
    }
}
