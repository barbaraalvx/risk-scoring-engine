package com.antifraude.quarantine.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//Classe responsável por Simular a comunicação com o backend do jogo.
@Component
public class GameBackendClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GameBackendClient.class);

    /**
     * Simula o bloqueio de um jogador. Protegido por circuit breaker: em caso
     * de falha ou circuito aberto, {@link #blockPlayerFallback(String, Throwable)}
     * é acionado.
     *
     * @param playerId ID do jogador a ser bloqueado.
     */
    @CircuitBreaker(name = "game-backend", fallbackMethod = "blockPlayerFallback")
    public void blockPlayer(final String playerId) {

        LOGGER.info(
            "[GAME BACKEND] Bloqueando jogador {}...",
            playerId
        );

        // Simulação de chamada externa.

    }

    /**
     * Fallback acionado pelo circuit breaker "game-backend" quando a chamada
     * de bloqueio falha ou o circuito está aberto. Propaga uma exceção clara
     * para que a SAGA de quarentena acione a compensação.
     *
     * @param playerId ID do jogador que se tentava bloquear.
     * @param throwable Causa original da falha (ou abertura do circuito).
     */
    private void blockPlayerFallback(final String playerId, final Throwable throwable) {

        LOGGER.error(
            "[GAME BACKEND] Falha ao bloquear jogador {}. Circuit breaker acionado.",
            playerId,
            throwable
        );

        throw new GameBackendUnavailableException(
            "Backend do jogo indisponível para bloquear jogador " + playerId,
            throwable
        );
    }

    /**
     * Simula o desbloqueio de um jogador.
     *
     * @param playerId ID do jogador a ser desbloqueado.
     */
    public void unblockPlayer(final String playerId) {

        LOGGER.info(
            "[GAME BACKEND] Desbloqueando jogador {}...",
            playerId
        );

    }

}
