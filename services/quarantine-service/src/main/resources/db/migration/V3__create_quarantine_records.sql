CREATE TABLE IF NOT EXISTS quarantine_records (

    id UUID PRIMARY KEY,

    player_id VARCHAR(255) NOT NULL,

    event_id UUID NOT NULL,

    total_score INTEGER NOT NULL,

    status VARCHAR(30) NOT NULL,

    reason VARCHAR(255) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    resolved_at TIMESTAMP WITH TIME ZONE

);

CREATE INDEX idx_quarantine_player_id
ON quarantine_records(player_id);

CREATE INDEX idx_quarantine_event_id
ON quarantine_records(event_id);

CREATE INDEX idx_quarantine_status
ON quarantine_records(status);