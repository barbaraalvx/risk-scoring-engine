CREATE TABLE IF NOT EXISTS risk_scores (
    id UUID PRIMARY KEY,
    player_id VARCHAR(255) NOT NULL,
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    total_score INT NOT NULL,
    device_fingerprint_score INT NOT NULL,
    action_velocity_score INT NOT NULL,
    choice_pattern_score INT NOT NULL,
    multi_account_score INT NOT NULL,
    quarantine_triggered BOOLEAN NOT NULL DEFAULT FALSE,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_risk_scores_player_id ON risk_scores(player_id);
CREATE INDEX idx_risk_scores_calculated_at ON risk_scores(calculated_at);
