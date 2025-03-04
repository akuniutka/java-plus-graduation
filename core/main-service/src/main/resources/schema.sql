CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN      NOT NULL,
    title  VARCHAR(255) NOT NULL,
    CONSTRAINT compilations_title_ux UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS compilations_events
(
    compilation_id BIGINT NOT NULL,
    event_id       BIGINT NOT NULL,
    CONSTRAINT compilation_event_compilation_id_fk FOREIGN KEY (compilation_id) REFERENCES compilations (id)
);

CREATE TABLE IF NOT EXISTS subscriptions
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subscriber_id BIGINT NOT NULL,
    target_id     BIGINT NOT NULL,
    CONSTRAINT subscriptions_subscriber_target_ux UNIQUE (subscriber_id, target_id)
);
