package com.antifraude.riskscoring.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.antifraude.riskscoring.domain.PlayerScoreRecord;
import com.antifraude.riskscoring.model.GameEvent;
import com.antifraude.riskscoring.model.ScoreUpdatedEvent;
import com.antifraude.riskscoring.repository.PlayerScoreRepository;
import com.antifraude.riskscoring.service.ScoringEngine;
import com.antifraude.riskscoring.service.ScoringWeights;
import com.antifraude.riskscoring.service.ScoringWeightsService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consumidor Kafka de eventos de jogadores (player-actions).
 * Orquestra o cálculo de risco, grava no Postgres e emite o evento de score atualizado.
 */
@Component
public class GameEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameEventConsumer.class);

    private final ScoringWeightsService weightsService;
    private final ScoringEngine scoringEngine;
    private final PlayerScoreRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String outputTopic;

    /**
     * Construtor injetando dependências.
     *
     * @param weightsService Serviço de Feature Flags Redis.
     * @param scoringEngine  Motor de scoring.
     * @param repository     Repositório PostgreSQL.
     * @param kafkaTemplate  Template Kafka.
     * @param objectMapper   ObjectMapper JSON.
     * @param outputTopic    Tópico de saída.
     */
    public GameEventConsumer(
            final ScoringWeightsService weightsService,
            final ScoringEngine scoringEngine,
            final PlayerScoreRepository repository,
            final KafkaTemplate<String, Object> kafkaTemplate,
            final ObjectMapper objectMapper,
            @Value("${scoring.topics.output-scores:risk-score-updated}") final String outputTopic) {
        this.weightsService = weightsService;
        this.scoringEngine = scoringEngine;
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.outputTopic = outputTopic;
    }

    /**
     * Escuta mensagens do tópico de ações de jogadores.
     *
     * @param rawMessage Mensagem bruta do Kafka.
     */
    @KafkaListener(topics = "${scoring.topics.input-events:player-actions}")
    @Transactional
    public void consume(final String rawMessage) {
        try {
            String jsonContent = rawMessage;
            if (jsonContent.startsWith("\"") && jsonContent.endsWith("\"")) {
                jsonContent = objectMapper.readValue(jsonContent, String.class);
            }

            GameEvent event = objectMapper.readValue(jsonContent, GameEvent.class);
            LOGGER.info("Consumindo evento do jogador: {} [tipo: {}]", event.playerId(), event.eventType());

            ScoringWeights weights = weightsService.getWeights();
            PlayerScoreRecord record = scoringEngine.calculate(event, weights);

            repository.save(record);

            ScoreUpdatedEvent scoreUpdated = new ScoreUpdatedEvent(
                    record.getEventId(),
                    record.getPlayerId(),
                    record.getTotalScore(),
                    weights.quarantineThreshold(),
                    record.getCalculatedAt()
            );

            kafkaTemplate.send(outputTopic, record.getPlayerId(), scoreUpdated);
            LOGGER.info("Score calculado e publicado para jogador {}: {} (Quarentena? {})",
                    record.getPlayerId(), record.getTotalScore(), record.isQuarantineTriggered());
        } catch (Exception e) {
            LOGGER.error("Erro ao processar mensagem do Kafka: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Falha no parse da mensagem", e);
        }
    }
}
