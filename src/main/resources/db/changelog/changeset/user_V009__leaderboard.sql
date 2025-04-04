CREATE TABLE IF NOT EXISTS user_activity(
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    user_id bigint,
    last_updated timestamptz DEFAULT current_timestamp,
    rating bigint,

    CONSTRAINT fk_user_activity_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS user_popularity(
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    user_id bigint,
    last_updated timestamptz DEFAULT current_timestamp,
    impact bigint,

    CONSTRAINT fk_user_popularity_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);