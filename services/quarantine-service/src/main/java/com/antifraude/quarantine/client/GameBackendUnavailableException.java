package com.antifraude.quarantine.client;

/**
 * Lançada quando o backend do jogo está indisponível, seja por falha direta
 * na chamada, seja pela abertura do circuit breaker "game-backend".
 */
public class GameBackendUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com uma mensagem e a causa raiz.
     *
     * @param message Mensagem descrevendo a falha.
     * @param cause   Causa original da falha (ou abertura do circuito).
     */
    public GameBackendUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
