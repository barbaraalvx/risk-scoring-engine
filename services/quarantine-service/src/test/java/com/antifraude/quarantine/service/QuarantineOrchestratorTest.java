package com.antifraude.quarantine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.antifraude.quarantine.client.GameBackendClient;
import com.antifraude.quarantine.client.GameBackendUnavailableException;
import com.antifraude.quarantine.domain.QuarantineStatus;
import com.antifraude.quarantine.event.QuarantineUpdatedEvent;
import com.antifraude.quarantine.event.ScoreUpdatedEvent;
import com.antifraude.quarantine.model.QuarantineRecord;
import com.antifraude.quarantine.readmodel.QuarantineProjectionService;
import com.antifraude.quarantine.repository.QuarantineRepository;

@ExtendWith(MockitoExtension.class)
class QuarantineOrchestratorTest {

    private static final String OUTPUT_TOPIC = "quarantine-updated";

    @Mock
    private QuarantineRepository repository;

    @Mock
    private KafkaTemplate<String, QuarantineUpdatedEvent> kafkaTemplate;

    @Mock
    private GameBackendClient gameBackendClient;

    @Mock
    private QuarantineProjectionService projectionService;

    private QuarantineOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new QuarantineOrchestrator(
                repository,
                kafkaTemplate,
                gameBackendClient,
                projectionService,
                OUTPUT_TOPIC);
    }

    @Test
    void shouldNoOpWhenEventAlreadyProcessed() {
        ScoreUpdatedEvent event = buildEvent(90, 80);
        when(repository.existsByEventId(event.eventId())).thenReturn(true);

        orchestrator.handle(event);

        verify(repository, never()).save(any(QuarantineRecord.class));
        verify(kafkaTemplate, never()).send(any(String.class), any(String.class), any(QuarantineUpdatedEvent.class));
    }

    @Test
    void shouldNotCreateRecordWhenScoreBelowThreshold() {
        ScoreUpdatedEvent event = buildEvent(10, 80);
        when(repository.existsByEventId(event.eventId())).thenReturn(false);

        orchestrator.handle(event);

        verify(repository, never()).save(any(QuarantineRecord.class));
        verify(gameBackendClient, never()).blockPlayer(any(String.class));
    }

    @Test
    void shouldQuarantinePlayerAndPublishEventOnSuccess() {
        ScoreUpdatedEvent event = buildEvent(90, 80);
        when(repository.existsByEventId(event.eventId())).thenReturn(false);

        orchestrator.handle(event);

        verify(gameBackendClient, times(1)).blockPlayer(eq(event.playerId()));
        verify(gameBackendClient, never()).unblockPlayer(any(String.class));

        ArgumentCaptor<QuarantineRecord> recordCaptor = ArgumentCaptor.forClass(QuarantineRecord.class);
        verify(repository, times(2)).save(recordCaptor.capture());
        assertEquals(QuarantineStatus.PENDING, recordCaptor.getAllValues().get(0).getStatus());
        assertEquals(QuarantineStatus.QUARANTINED, recordCaptor.getAllValues().get(1).getStatus());

        ArgumentCaptor<QuarantineUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(QuarantineUpdatedEvent.class);
        verify(kafkaTemplate, times(1)).send(eq(OUTPUT_TOPIC), eq(event.playerId()), eventCaptor.capture());
        assertEquals(QuarantineStatus.QUARANTINED, eventCaptor.getValue().status());

        verify(projectionService, times(2)).project(any(QuarantineRecord.class));
    }

    @Test
    void shouldCompensateAndEndInCompensatedStateWhenGameBackendFails() {
        ScoreUpdatedEvent event = buildEvent(90, 80);
        when(repository.existsByEventId(event.eventId())).thenReturn(false);
        doThrow(new GameBackendUnavailableException("Backend indisponível", new RuntimeException("timeout")))
                .when(gameBackendClient).blockPlayer(event.playerId());

        orchestrator.handle(event);

        verify(gameBackendClient, times(1)).unblockPlayer(eq(event.playerId()));

        ArgumentCaptor<QuarantineRecord> recordCaptor = ArgumentCaptor.forClass(QuarantineRecord.class);
        verify(repository, times(3)).save(recordCaptor.capture());
        assertEquals(QuarantineStatus.PENDING, recordCaptor.getAllValues().get(0).getStatus());
        assertEquals(QuarantineStatus.FAILED, recordCaptor.getAllValues().get(1).getStatus());
        assertEquals(QuarantineStatus.COMPENSATED, recordCaptor.getAllValues().get(2).getStatus());

        ArgumentCaptor<QuarantineUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(QuarantineUpdatedEvent.class);
        verify(kafkaTemplate, times(1)).send(eq(OUTPUT_TOPIC), eq(event.playerId()), eventCaptor.capture());
        assertEquals(QuarantineStatus.COMPENSATED, eventCaptor.getValue().status());

        verify(projectionService, times(3)).project(any(QuarantineRecord.class));
    }

    private ScoreUpdatedEvent buildEvent(final int totalScore, final int threshold) {
        return new ScoreUpdatedEvent(
                UUID.randomUUID(),
                "player-" + UUID.randomUUID(),
                totalScore,
                threshold,
                Instant.now());
    }
}
