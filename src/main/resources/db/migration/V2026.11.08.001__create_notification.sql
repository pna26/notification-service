-- uma linha por evento já tratado: a memória de idempotência do consumidor
CREATE TABLE processed_events (
    event_id     UUID        PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- uma linha por e-mail realmente enviado (sem UNIQUE: queremos VER duplicatas
-- quando a idempotência é desligada no teste)
CREATE TABLE notifications (
    id          BIGSERIAL    PRIMARY KEY,
    event_id    UUID         NOT NULL,
    account_id  VARCHAR(36)  NOT NULL,
    email       VARCHAR(256) NOT NULL,
    kind        VARCHAR(50)  NOT NULL,
    occurred_at TIMESTAMPTZ  NOT NULL,
    sent_at     TIMESTAMPTZ  NOT NULL DEFAULT clock_timestamp()
);

-- mensagens envenenadas, guardadas para inspeção e reprocessamento
CREATE TABLE dead_letters (
    id            BIGSERIAL   PRIMARY KEY,
    topic         TEXT        NOT NULL,
    partition     INT         NOT NULL,
    record_offset BIGINT      NOT NULL,
    record_key    TEXT,
    payload       TEXT,
    reason        TEXT,
    received_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
