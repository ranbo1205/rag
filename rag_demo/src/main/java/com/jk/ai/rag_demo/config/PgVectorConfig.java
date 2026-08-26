package com.jk.ai.rag_demo.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PgVectorConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
           @Value("${spring.datasource.password}") String password) {

        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("rag_db")
                .user("postgres")
                .password(password)
                .table("knowledge_vector")
                .dimension(1024)
                .createTable(true)
                .dropTableFirst(false)
                .build();


    };
}
