-- Initialize pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create table with LangChain4j compatible schema (300 dimensions)
CREATE TABLE IF NOT EXISTS test_index (
    embedding_id UUID PRIMARY KEY,
    embedding vector(768),
    text TEXT,
    metadata JSONB
);

-- Grant permissions to the admin user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO admin;