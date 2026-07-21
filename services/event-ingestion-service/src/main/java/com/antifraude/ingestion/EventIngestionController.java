package com.antifraude.ingestion;

import com.antifraude.ingestion.model.GameEvent;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventIngestionController {

    private final EventPublisher eventPublisher;

    public EventIngestionController(final EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventAcceptedResponse ingest(@Valid @RequestBody final GameEvent gameEvent) {
        // Normaliza o comando de entrada: garante eventId para rastreabilidade idempotente no pipeline.
        GameEvent normalizedEvent = gameEvent.withGeneratedEventId();

        // Este servico e stateless: valida e roteia, sem persistir estado de negocio localmente.
        eventPublisher.publish(normalizedEvent);

        // 202 indica processamento aceito para fluxo assincrono (Kafka), nao concluido integralmente.
        return new EventAcceptedResponse(normalizedEvent.eventId());
    }
}
