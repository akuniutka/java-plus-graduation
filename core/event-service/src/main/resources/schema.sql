CREATE TABLE IF NOT EXISTS categories
(
  id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  CONSTRAINT categories_name_ux2 UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS events
(
  id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  initiator_id       BIGINT                      NOT NULL,
  title              VARCHAR(120)                NOT NULL,
  category_id        BIGINT                      NOT NULL,
  event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  lat                REAL,
  lon                REAL,
  annotation         VARCHAR(2000)               NOT NULL,
  description        VARCHAR(7000)               NOT NULL,
  participant_limit  BIGINT                      NOT NULL,
  paid               BOOLEAN                     NOT NULL,
  request_moderation BOOLEAN                     NOT NULL,
  created_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  published_on       TIMESTAMP WITHOUT TIME ZONE,
  state              VARCHAR(10)                 NOT NULL,
  CONSTRAINT events_category_id_fk FOREIGN KEY (category_id) REFERENCES categories (id),
  CONSTRAINT state_values CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED'))
);
