package com.antifraude.riskscoring.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.antifraude.riskscoring.domain.PlayerScoreRecord;
import com.antifraude.riskscoring.repository.PlayerScoreRepository;
import com.antifraude.riskscoring.service.ScoringWeights;
import com.antifraude.riskscoring.service.ScoringWeightsService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * Controller expondo endpoints REST de consulta de scores (Query Side CQRS) e Feature Flags Admin.
 */
@RestController
@RequestMapping("/api/v1")
public class ScoreController {

    private final PlayerScoreRepository repository;
    private final ScoringWeightsService weightsService;

    /**
     * Construtor injetando dependências.
     *
     * @param repository     Repositório JPA.
     * @param weightsService Serviço de Feature Flags Redis.
     */
    public ScoreController(final PlayerScoreRepository repository, final ScoringWeightsService weightsService) {
        this.repository = repository;
        this.weightsService = weightsService;
    }

    /**
     * Retorna o score mais recente do jogador.
     *
     * @param playerId ID do jogador.
     * @return PlayerScoreRecord mais recente.
     */
    @GetMapping("/scores/{playerId}")
    @CircuitBreaker(name = "postgres", fallbackMethod = "scoreFallback")
    public PlayerScoreRecord getLatestScore(@PathVariable final String playerId) {
        return repository.findFirstByPlayerIdOrderByCalculatedAtDesc(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogador não encontrado no histórico"));
    }

    /**
     * Retorna o histórico de scores de um jogador (paginado).
     *
     * @param playerId ID do jogador.
     * @param limit    Quantidade limite de registros (default 20).
     * @return Lista de PlayerScoreRecord.
     */
    @GetMapping("/scores/{playerId}/history")
    @CircuitBreaker(name = "postgres", fallbackMethod = "historyFallback")
    public List<PlayerScoreRecord> getScoreHistory(
            @PathVariable final String playerId,
            @RequestParam(defaultValue = "20") final int limit) {
        List<PlayerScoreRecord> history = repository.findByPlayerIdOrderByCalculatedAtDesc(
                playerId, PageRequest.of(0, Math.min(100, Math.max(1, limit))));

        if (history.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogador não encontrado no histórico");
        }
        return history;
    }

    /**
     * Lista as Feature Flags e pesos de scoring atualmente ativos.
     *
     * @return Instância de ScoringWeights.
     */
    @GetMapping("/flags")
    public ScoringWeights getFlags() {
        return weightsService.getWeights();
    }

    /**
     * Atualiza os pesos e thresholds no Redis em tempo real (HOT-RELOAD).
     *
     * @param newWeights Novos pesos informados pelo Admin.
     * @return ScoringWeights atualizados.
     */
    @PutMapping("/admin/scoring-weights")
    @ResponseStatus(HttpStatus.OK)
    public ScoringWeights updateWeights(@RequestBody final ScoringWeights newWeights) {
        weightsService.updateWeights(newWeights);
        return newWeights;
    }

    /**
     * Fallback do Circuit Breaker para a consulta de score mais recente.
     */
    public PlayerScoreRecord scoreFallback(final String playerId, final Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível no momento");
    }

    /**
     * Fallback do Circuit Breaker para a consulta de histórico.
     */
    public List<PlayerScoreRecord> historyFallback(final String playerId, final int limit, final Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível no momento");
    }
}
