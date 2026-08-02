package com.antifraude.riskscoring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * Serviço responsável por gerenciar Feature Flags e pesos de scoring armazenados no Redis.
 * Protegido com Resilience4j Circuit Breaker.
 */
@Service
public class ScoringWeightsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoringWeightsService.class);
    private static final String REDIS_KEY = "scoring:weights";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Construtor injetando dependências.
     *
     * @param redisTemplate Template do Redis.
     * @param objectMapper  Mapper JSON.
     */
    public ScoringWeightsService(final StringRedisTemplate redisTemplate, final ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Lê os pesos configurados no Redis com proteção de Circuit Breaker e fallback.
     *
     * @return Configuração de pesos imutável.
     */
    @CircuitBreaker(name = "redis", fallbackMethod = "defaultWeights")
    public ScoringWeights getWeights() {
        String json = redisTemplate.opsForValue().get(REDIS_KEY);
        if (json == null || json.isBlank()) {
            return ScoringWeights.defaultConfig();
        }
        try {
            return objectMapper.readValue(json, ScoringWeights.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Falha ao desserializar pesos do Redis, usando valores default. Erro: {}", e.getMessage());
            return ScoringWeights.defaultConfig();
        }
    }

    /**
     * Atualiza os pesos no Redis em tempo real (Feature Flag HOT-RELOAD).
     *
     * @param weights Novos pesos.
     */
    @CircuitBreaker(name = "redis", fallbackMethod = "updateFallback")
    public void updateWeights(final ScoringWeights weights) {
        try {
            String json = objectMapper.writeValueAsString(weights);
            redisTemplate.opsForValue().set(REDIS_KEY, json);
            LOGGER.info("Pesos de scoring atualizados com sucesso no Redis: {}", json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Erro ao serializar novos pesos de scoring", e);
        }
    }

    /**
     * Fallback executado quando o Circuit Breaker do Redis está ABERTO ou em falha.
     *
     * @param ex Exceção geradora.
     * @return Configuração padrão.
     */
    public ScoringWeights defaultWeights(final Throwable ex) {
        LOGGER.warn("Circuit Breaker 'redis' acionado! Retornando pesos default. Motivo: {}", ex.getMessage());
        return ScoringWeights.defaultConfig();
    }

    /**
     * Fallback para a atualização de pesos.
     *
     * @param weights Novos pesos.
     * @param ex      Exceção geradora.
     */
    public void updateFallback(final ScoringWeights weights, final Throwable ex) {
        LOGGER.error(
                "Circuit Breaker 'redis' acionado! Não foi possível atualizar os pesos no Redis. Motivo: {}",
                ex.getMessage());
        throw new IllegalStateException("Serviço de Redis indisponível no momento", ex);
    }
}
