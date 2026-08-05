package com.antifraude.quarantine.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antifraude.quarantine.model.QuarantineRecord;
import com.antifraude.quarantine.readmodel.QuarantineProjectionService;
import com.antifraude.quarantine.readmodel.QuarantineView;
import com.antifraude.quarantine.repository.QuarantineRepository;

/**
 * Endpoints de consulta (lado de leitura, CQRS) para o painel administrativo.
 */
@RestController
@RequestMapping("/quarantine")
public class QuarantineQueryController {

    private final QuarantineProjectionService projectionService;
    private final QuarantineRepository repository;

    /**
     * Cria o controller de consulta de quarentena.
     *
     * @param projectionService Serviço de leitura do read model em Redis.
     * @param repository Repositório JPA usado para o histórico auditável.
     */
    public QuarantineQueryController(
            final QuarantineProjectionService projectionService,
            final QuarantineRepository repository) {

        this.projectionService = projectionService;
        this.repository = repository;
    }

    /**
     * Retorna os registros mais recentes de quarentena para o painel administrativo.
     *
     * @return 200 com a lista dos registros de quarentena mais recentes.
     */
    @GetMapping
    public ResponseEntity<List<QuarantineRecord>> getAllRecentQuarantines() {
        return ResponseEntity.ok(
                repository.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());
    }


    /**
     * Retorna o estado atual da quarentena de um jogador a partir do read
     * model em Redis (hot path do CQRS, sem tocar o Postgres).
     *
     * @param playerId ID do jogador.
     * @return 200 com o estado atual, ou 404 caso não exista projeção.
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<QuarantineView> getCurrentStatus(@PathVariable final String playerId) {
        return projectionService.findByPlayerId(playerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retorna o histórico paginado de quarentenas de um jogador a partir do
     * Postgres (trilha de auditoria do write model).
     *
     * @param playerId ID do jogador.
     * @param pageable Configuração de paginação.
     * @return 200 com a lista de registros históricos.
     */
    @GetMapping("/{playerId}/history")
    public ResponseEntity<List<QuarantineRecord>> getHistory(
            @PathVariable final String playerId,
            final Pageable pageable) {

        return ResponseEntity.ok(
                repository.findByPlayerIdOrderByCreatedAtDesc(playerId, pageable));
    }
}
