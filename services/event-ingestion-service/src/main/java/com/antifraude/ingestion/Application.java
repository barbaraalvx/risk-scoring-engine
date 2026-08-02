package com.antifraude.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação Spring Boot de Ingestão de Eventos.
 */
@SpringBootApplication
public class Application {

    /**
     * Construtor padrão protegido.
     */
    protected Application() {
    }

    /**
     * Método principal que inicializa o Spring Boot.
     *
     * @param args Argumentos de linha de comando.
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
