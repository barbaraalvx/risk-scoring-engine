package com.antifraude.ingestion.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.antifraude.ingestion.domain.PlayerEventEntity;

/**
 * Repositório JPA para eventos no Event Store (PostgreSQL).
 */
@Repository
public interface PlayerEventRepository extends JpaRepository<PlayerEventEntity, UUID> {

    /**
     * Busca histórico de eventos de um jogador ordenado por timestamp decrescente.
     *
     * @param playerId ID do jogador.
     * @return Lista de entidades de evento.
     */
    List<PlayerEventEntity> findByPlayerIdOrderByTimestampDesc(String playerId);
}
