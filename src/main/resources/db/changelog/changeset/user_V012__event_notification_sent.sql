CREATE TABLE event_notification_sent (
    event_id BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    interval_minutes INT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (event_id, interval_minutes)
);
