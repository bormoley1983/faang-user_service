ALTER TABLE user_premium
    ADD CONSTRAINT uq_user_premium_user UNIQUE (user_id);

CREATE TABLE premium_purchase_intent (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    idempotency_key UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    premium_period VARCHAR(32) NOT NULL,
    payment_number BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    premium_id BIGINT UNIQUE REFERENCES user_premium(id),
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
