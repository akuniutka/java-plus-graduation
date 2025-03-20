CREATE TABLE IF NOT EXISTS user_scores
(
  id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id  BIGINT    NOT NULL,
  user_id   BIGINT    NOT NULL,
  score     REAL      NOT NULL,
  timestamp TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS similarity_scores
(
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_a_id BIGINT    NOT NULL,
  event_b_id BIGINT    NOT NULL,
  score      REAL      NOT NULL,
  timestamp  TIMESTAMP NOT NULL
);
