package com.antifraude.quarantine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.antifraude.quarantine.domain.QuarantineStatus;
import com.antifraude.quarantine.model.QuarantineRecord;
import com.antifraude.quarantine.readmodel.QuarantineProjectionService;
import com.antifraude.quarantine.readmodel.QuarantineView;
import com.antifraude.quarantine.repository.QuarantineRepository;

@ExtendWith(MockitoExtension.class)
class QuarantineQueryControllerTest {

    private static final String PLAYER_ID = "player-1";

    @Mock
    private QuarantineProjectionService projectionService;

    @Mock
    private QuarantineRepository repository;

    private QuarantineQueryController controller;

    @BeforeEach
    void setUp() {
        controller = new QuarantineQueryController(projectionService, repository);
    }

    @Test
    void shouldReturnCurrentStatusFromRedisWhenPresent() {
        QuarantineView view = new QuarantineView(
                PLAYER_ID, QuarantineStatus.QUARANTINED, "score alto", 90, Instant.now());
        when(projectionService.findByPlayerId(PLAYER_ID)).thenReturn(Optional.of(view));

        ResponseEntity<QuarantineView> response = controller.getCurrentStatus(PLAYER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(view, response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenNoProjectionExists() {
        when(projectionService.findByPlayerId(PLAYER_ID)).thenReturn(Optional.empty());

        ResponseEntity<QuarantineView> response = controller.getCurrentStatus(PLAYER_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnHistoryFromPostgres() {
        Pageable pageable = PageRequest.of(0, 10);
        QuarantineRecord record = new QuarantineRecord(
                UUID.randomUUID(), PLAYER_ID, UUID.randomUUID(), 90,
                QuarantineStatus.QUARANTINED, "score alto", Instant.now(), Instant.now());
        when(repository.findByPlayerIdOrderByCreatedAtDesc(eq(PLAYER_ID), any(Pageable.class)))
                .thenReturn(List.of(record));

        ResponseEntity<List<QuarantineRecord>> response = controller.getHistory(PLAYER_ID, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
