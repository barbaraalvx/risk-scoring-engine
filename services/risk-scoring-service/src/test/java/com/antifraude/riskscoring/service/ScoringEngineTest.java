package com.antifraude.riskscoring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.antifraude.riskscoring.domain.PlayerScoreRecord;
import com.antifraude.riskscoring.model.GameEvent;
import com.antifraude.riskscoring.repository.PlayerScoreRepository;
import com.antifraude.riskscoring.service.rules.ActionVelocityRule;
import com.antifraude.riskscoring.service.rules.ChoicePatternRule;
import com.antifraude.riskscoring.service.rules.DeviceFingerprintRule;
import com.antifraude.riskscoring.service.rules.MultiAccountRule;

@ExtendWith(MockitoExtension.class)
class ScoringEngineTest {

    @Mock
    private PlayerScoreRepository repository;

    private ScoringEngine scoringEngine;

    @BeforeEach
    void setUp() {
        DeviceFingerprintRule deviceRule = new DeviceFingerprintRule();
        ActionVelocityRule velocityRule = new ActionVelocityRule(repository);
        ChoicePatternRule patternRule = new ChoicePatternRule();
        MultiAccountRule multiAccountRule = new MultiAccountRule();

        scoringEngine = new ScoringEngine(deviceRule, velocityRule, patternRule, multiAccountRule);
    }

    @Test
    void shouldCalculateLowRiskForNormalPlayer() {
        GameEvent event = new GameEvent(
                "evt-1",
                "player-normal",
                "BET",
                Instant.now().toString(),
                "sess-1",
                "fp-valid-phone",
                "192.168.1.50",
                Map.of("amount", 50.0)
        );

        when(repository.countActionsSince(eq("player-normal"), any(Instant.class))).thenReturn(0L);

        ScoringWeights weights = ScoringWeights.defaultConfig();
        PlayerScoreRecord scoreRecord = scoringEngine.calculate(event, weights);

        assertNotNull(scoreRecord);
        assertEquals(0, scoreRecord.getTotalScore());
        assertFalse(scoreRecord.isQuarantineTriggered());
    }

    @Test
    void shouldCalculateHighRiskAndTriggerQuarantineForBotWithEmulatedDevice() {
        GameEvent event = new GameEvent(
                "evt-2",
                "player-bot",
                "MULTI_ACCOUNT_SUSPECT",
                Instant.now().toString(),
                "sess-2",
                "android_emulator",
                "10.0.0.5",
                Map.of("amount", 10000.0)
        );

        when(repository.countActionsSince(eq("player-bot"), any(Instant.class))).thenReturn(10L);

        ScoringWeights weights = ScoringWeights.defaultConfig();
        PlayerScoreRecord scoreRecord = scoringEngine.calculate(event, weights);

        assertNotNull(scoreRecord);
        assertEquals(100, scoreRecord.getTotalScore());
        assertTrue(scoreRecord.isQuarantineTriggered());
    }

    @Test
    void shouldRespectAdminFlagsWhenRulesAndQuarantineAreDisabled() {
        GameEvent event = new GameEvent(
                "evt-3",
                "player-flagged",
                "MULTI_ACCOUNT_SUSPECT",
                Instant.now().toString(),
                "sess-3",
                "android_emulator",
                "10.0.0.6",
                Map.of("amount", 10000.0)
        );

        when(repository.countActionsSince(eq("player-flagged"), any(Instant.class))).thenReturn(10L);

        ScoringWeights weights = new ScoringWeights(
                0.25,
                0.25,
                0.25,
                0.25,
                70.0,
                false,
                false,
                false,
                false,
                false,
                false);

        PlayerScoreRecord scoreRecord = scoringEngine.calculate(event, weights);

        assertNotNull(scoreRecord);
        assertEquals(0, scoreRecord.getTotalScore());
        assertFalse(scoreRecord.isQuarantineTriggered());
    }
}
