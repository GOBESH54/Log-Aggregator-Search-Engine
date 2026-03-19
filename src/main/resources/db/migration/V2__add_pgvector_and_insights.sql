CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS log_embeddings (
    id BIGSERIAL PRIMARY KEY,
    log_id BIGINT NOT NULL,
    log_timestamp TIMESTAMP NOT NULL,
    embedding vector(768) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_embeddings_logs
        FOREIGN KEY (log_id, log_timestamp)
        REFERENCES logs (id, timestamp)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_log_embeddings_log_ref
    ON log_embeddings (log_id, log_timestamp);

CREATE INDEX IF NOT EXISTS idx_log_embeddings_vector_ivfflat
    ON log_embeddings
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE TABLE IF NOT EXISTS ai_insights (
    id BIGSERIAL PRIMARY KEY,
    insight_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    summary TEXT NOT NULL,
    recommended_action TEXT,
    raw_response TEXT,
    analysis_window_start TIMESTAMP NOT NULL,
    analysis_window_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ai_insights_severity
        CHECK (severity IN ('HIGH', 'MEDIUM', 'LOW'))
);

CREATE INDEX IF NOT EXISTS idx_ai_insights_created_at
    ON ai_insights (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_insights_severity
    ON ai_insights (severity);
