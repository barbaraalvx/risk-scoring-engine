package com.antifraude.quarantine.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.antifraude.quarantine.model.QuarantineRecord;

/**
 * Repositório JPA para registros de quarentena.
 */
@Repository
public interface QuarantineRepository extends JpaRepository<QuarantineRecord, UUID> {

    /**
     * Busca a quarentena mais recente de um jogador.
     *
     * @param playerId ID do jogador.
     * @return Optional contendo o registro mais recente.
     */
    Optional<QuarantineRecord> findFirstByPlayerIdOrderByCreatedAtDesc(String playerId);

    /**
     * Busca o histórico paginado de quarentenas de um jogador.
     *
     * @param playerId ID do jogador.
     * @param pageable Configuração de paginação.
     * @return Lista de registros.
     */
    List<QuarantineRecord> findByPlayerIdOrderByCreatedAtDesc(
            String playerId,
            Pageable pageable);

    /**
     * Busca um registro de quarentena pelo evento que o originou.
     *
     * @param eventId ID do evento de score.
     * @return Optional contendo a quarentena.
     */
    Optional<QuarantineRecord> findByEventId(UUID eventId);

    /**
     * Verifica se já existe uma quarentena associada ao evento informado.
     *
     * @param eventId ID do evento.
     * @return true caso exista.
     */
    boolean existsByEventId(UUID eventId);
}