-- USR-18: prevent duplicate rows in relation tables that the app treats as unique.

ALTER TABLE subscription
    ADD CONSTRAINT uq_subscription_follower_followee UNIQUE (follower_id, followee_id);

ALTER TABLE user_event
    ADD CONSTRAINT uq_user_event_user_event UNIQUE (user_id, event_id);
