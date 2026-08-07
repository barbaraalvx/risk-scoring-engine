package com.antifraude.riskscoring.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.antifraude.riskscoring.domain.PlayerScoreRecord;

/**
 * Repositório JPA para registros de score de risco.
 */
@Repository
public interface PlayerScoreRepository extends JpaRepository<PlayerScoreRecord, UUID> {

    /**
     * Busca o registro de score mais recente de um jogador.
     *
     * @param playerId ID do jogador.
     * @return Optional do último PlayerScoreRecord.
     */
    Optional<PlayerScoreRecord> findFirstByPlayerIdOrderByCalculatedAtDesc(String playerId);

    /**
     * Busca o histórico paginado de scores de um jogador.
     *
     * @param playerId ID do jogador.
     * @param pageable Configuração de paginação/limite.
     * @return Lista de registros de score.
     */
    List<PlayerScoreRecord> findByPlayerIdOrderByCalculatedAtDesc(String playerId, Pageable pageable);

    /**
     * Conta ações do mesmo jogador dentro de uma janela de tempo (velocidade).
     *
     * @param playerId ID do jogador.
     * @param since    Início da janela de tempo.
     * @return Quantidade de ações.
     */
    @Query("SELECT COUNT(r) FROM PlayerScoreRecord r WHERE r.playerId = :playerId AND r.calculatedAt >= :since")
    long countActionsSince(@Param("playerId") String playerId, @Param("since") Instant since);

    /**
     * Conta quantos registros de score dispararam quarentena.
     *
     * @return Total de registros com quarentena acionada.
     */
    long countByQuarantineTriggeredTrue();

    /**
     * Busca os registros mais recentes para exibição no painel admin.
     *
     * @return Lista com os 10 registros mais recentes.
     */
    List<PlayerScoreRecord> findTop10ByOrderByCalculatedAtDesc();
}
