-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建知识库向量表（1024 维，对应通义 text-embedding-v3）
CREATE TABLE IF NOT EXISTS knowledge_vector (
                                                id UUID PRIMARY KEY,
                                                content TEXT NOT NULL,
                                                embedding VECTOR(1024),
    metadata JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- IVFFlat 索引：加速余弦相似度检索
CREATE INDEX IF NOT EXISTS idx_knowledge_vector_embedding
    ON knowledge_vector
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- GIN 索引：加速 metadata 字段过滤（如按 category 筛选）
CREATE INDEX IF NOT EXISTS idx_knowledge_vector_metadata
    ON knowledge_vector
    USING gin (metadata);

COMMENT ON TABLE knowledge_vector IS 'RAG系统知识库向量表';