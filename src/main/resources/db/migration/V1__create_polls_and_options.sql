CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

SET TIMEZONE='UTC';

CREATE TABLE polls(
   id        VARCHAR(255) NOT NULL,
   question  TEXT NOT NULL,
   starts_at TIMESTAMPTZ NOT NULL,
   ends_at   TIMESTAMPTZ NOT NULL,
   CONSTRAINT polls_pk_id PRIMARY KEY (id)
);

CREATE TABLE options(
   id      VARCHAR(255) NOT NULL,
   text    TEXT NOT NULL,
   votes   INTEGER NOT NULL DEFAULT 0,
   poll_id VARCHAR(255) NOT NULL,
   CONSTRAINT options_pk_id PRIMARY KEY (id),
   CONSTRAINT options_fk_poll FOREIGN KEY (poll_id) REFERENCES polls (id) ON DELETE CASCADE
);

CREATE INDEX options_idx_fk_poll ON options(poll_id);
