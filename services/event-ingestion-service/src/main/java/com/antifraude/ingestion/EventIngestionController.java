package com.antifraude.ingestion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.antifraude.ingestion.model.GameEvent;

import jakarta.validation.Valid;

/**
 * Controller de ingestão de eventos de ações de jogadores.
 * Mapeia /api/v1/actions conforme especificação REST.
 */
@RestController
@RequestMapping("/api/v1")
public class EventIngestionController {

    private final IngestionService ingestionService;

    /**
     * Construtor injetando IngestionService.
     *
     * @param ingestionService Serviço de ingestão.
     */
    public EventIngestionController(final IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Endpoint para receber ações de jogadores.
     *
     * @param gameEvent Evento recebido.
     * @return EventAcceptedResponse com o ID gerado/atribuído.
     */
    @PostMapping("/actions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventAcceptedResponse ingestAction(@Valid @RequestBody final GameEvent gameEvent) {
        GameEvent processedEvent = ingestionService.ingestAndPublish(gameEvent);
        return new EventAcceptedResponse(processedEvent.eventId());
    }

    /**
     * Endpoint legado mantido para retrocompatibilidade (/events).
     *
     * @param gameEvent Evento recebido.
     * @return EventAcceptedResponse.
     */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventAcceptedResponse ingestLegacy(@Valid @RequestBody final GameEvent gameEvent) {
        GameEvent processedEvent = ingestionService.ingestAndPublish(gameEvent);
        return new EventAcceptedResponse(processedEvent.eventId());
    }
}
