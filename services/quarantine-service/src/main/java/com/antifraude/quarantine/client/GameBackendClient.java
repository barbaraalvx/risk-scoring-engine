package com.antifraude.quarantine.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//Classe responsável por Simular a comunicação com o backend do jogo.
@Component
public class GameBackendClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GameBackendClient.class);

    /**
     * Simula o bloqueio de um jogador.
     */
    public void blockPlayer(final String playerId) {

        LOGGER.info(
            "[GAME BACKEND] Bloqueando jogador {}...",
            playerId
        );

        // Simulação de chamada externa.

    }

    /**
     * Simula o desbloqueio de um jogador.
     */
    public void unblockPlayer(final String playerId) {

        LOGGER.info(
            "[GAME BACKEND] Desbloqueando jogador {}...",
            playerId
        );

    }

}