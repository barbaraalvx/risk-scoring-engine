package com.antifraude.quarantine.readmodel;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.antifraude.quarantine.model.QuarantineRecord;

/**
 * Responsável por manter o read model de quarentena em Redis (CQRS),
 * projetando de forma assíncrona o estado mais recente do write model
 * (Postgres) para consulta rápida pelo painel administrativo.
 */
@Service
public class QuarantineProjectionService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(QuarantineProjectionService.class);

    private static final String KEY_PREFIX = "quarantine:view:";

    private final RedisTemplate<String, QuarantineView> redisTemplate;

    /**
     * Cria o serviço de projeção do read model de quarentena.
     *
     * @param redisTemplate Template usado para ler e escrever a projeção em Redis.
     */
    public QuarantineProjectionService(final RedisTemplate<String, QuarantineView> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atualiza a projeção em Redis com o estado mais recente do registro.
     *
     * @param record Registro de quarentena persistido no Postgres.
     */
    public void project(final QuarantineRecord record) {
        final Instant updatedAt =
                record.getResolvedAt() != null ? record.getResolvedAt() : record.getCreatedAt();

        final QuarantineView view = new QuarantineView(
                record.getPlayerId(),
                record.getStatus(),
                record.getReason(),
                record.getTotalScore(),
                updatedAt
        );

        redisTemplate.opsForValue().set(key(record.getPlayerId()), view);

        LOGGER.info(
                "Projeção de leitura atualizada em Redis para jogador {} com status {}.",
                record.getPlayerId(),
                record.getStatus()
        );
    }

    /**
     * Busca a projeção atual de um jogador diretamente em Redis.
     *
     * @param playerId ID do jogador.
     * @return Optional contendo o estado atual, se existir.
     */
    public Optional<QuarantineView> findByPlayerId(final String playerId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(playerId)));
    }

    private String key(final String playerId) {
        return KEY_PREFIX + playerId;
    }
}
