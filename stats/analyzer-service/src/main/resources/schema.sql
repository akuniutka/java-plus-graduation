CREATE TABLE IF NOT EXISTS user_scores
(
  id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id  BIGINT,
  user_id   BIGINT,
  score     REAL,
  timestamp TIMESTAMP
);

CREATE TABLE IF NOT EXISTS similarity_scores
(
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_a_id BIGINT,
  event_b_id BIGINT,
  score REAL,
  timestamp TIMESTAMP
);
