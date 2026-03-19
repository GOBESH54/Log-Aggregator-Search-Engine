-- Create partitioned logs table
CREATE TABLE IF NOT EXISTS logs (
    id BIGSERIAL,
    source VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    metadata TEXT,
    PRIMARY KEY (id, timestamp)
) PARTITION BY RANGE (timestamp);

-- Create GIN index for full-text search on message
CREATE INDEX IF NOT EXISTS idx_logs_message_fts ON logs USING GIN (to_tsvector('english', message));

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_logs_source ON logs(source);
CREATE INDEX IF NOT EXISTS idx_logs_level ON logs(level);
CREATE INDEX IF NOT EXISTS idx_logs_timestamp ON logs(timestamp DESC);
