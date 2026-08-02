CREATE TABLE IF NOT EXISTS player_events (
    id UUID PRIMARY KEY,
    player_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    session_id VARCHAR(255),
    device_fingerprint VARCHAR(255),
    ip_address VARCHAR(255),
    payload TEXT
);

CREATE INDEX idx_player_events_player_id ON player_events(player_id);
CREATE INDEX idx_player_events_timestamp ON player_events(timestamp);
