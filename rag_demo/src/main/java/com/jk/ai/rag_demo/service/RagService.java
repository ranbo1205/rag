package com.jk.ai.rag_demo.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class RagService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;
    private final Tracer tracer;

    public RagService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore, ChatModel chatModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.tracer = GlobalOpenTelemetry.getTracer("rag-service");
    }

    /**
     * 文档入库：文本切分 -> 向量化 -> 存入 PGVector
     * @param content 原始文档文本
     * @param source  来源标识（文件名/URL 等），写入 metadata
     * @return 切分出的 chunk 数量
     */
    public int ingest(String content, String source) {

        // 1. 文本切分：800 字符一块，相邻块重叠 150 字符（保证上下文连贯）
        List<TextSegment> segments = DocumentSplitters.recursive(800,150).split(Document.from(content));

        // 2. 逐块向量化并入库
        for (TextSegment segment : segments) {
            // 把 source 放进 metadata（LangChain4j 会序列化到 metadata JSONB 列）
            TextSegment enriched = TextSegment.from(segment.text(), Metadata.from("source", source) );

            Embedding embedding = embeddingModel.embed(enriched).content();

            embeddingStore.add(embedding, enriched);
        }
        return segments.size();
    }

    /**
     * RAG 问答：问题向量化 -> HNSW 检索 Top-K -> 拼接 Prompt -> DeepSeek 生成
     */
    public String chat(String question) {

        // ★ 根 span：rag.chat
        Span rootSpan = tracer.spanBuilder("rag.chat")
                .setAttribute("input.value", question)
                .startSpan();

        try (Scope rootScope = rootSpan.makeCurrent()) {

            // ---- 1. 问题向量化 ----
            Embedding questionEmbedding = embeddingModel.embed(question).content();

            // ---- 2. 构建检索请求 ----
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding)
                    .maxResults(5)
                    .minScore(0.5)
                    .build();

            // ---- 3. retrieval span（手动记录向量检索）----
            Span retrievalSpan = tracer.spanBuilder("retrieval")
                    .setAttribute("openinference.span.kind", "retriever")
                    .startSpan();

            EmbeddingSearchResult<TextSegment> searchResult;
            try (Scope retScope = retrievalSpan.makeCurrent()) {
                searchResult = embeddingStore.search(searchRequest);

                // 记录检索到的文档
                List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
                for (int i = 0; i < matches.size(); i++) {
                    retrievalSpan.setAttribute(
                            "retrieval.documents." + i + ".content",
                            matches.get(i).embedded().text()
                    );
                    retrievalSpan.setAttribute(
                            "retrieval.documents." + i + ".score",
                            matches.get(i).score()
                    );
                }
                retrievalSpan.setAttribute("retrieval.total_matches", matches.size());
            } finally {
                retrievalSpan.end();
            }

            // ---- 4. 拼接上下文 ----
            List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
            if (CollectionUtils.isEmpty(matches)) {
                rootSpan.setAttribute("output.value", "Sorry, no relevant information found.");
                return "Sorry, no relevant information found in the knowledge base.";
            }

            StringBuilder context = new StringBuilder();
            for (int i = 0; i < matches.size(); i++) {
                TextSegment seg = matches.get(i).embedded();
                String src = seg.metadata().getString("source");
                context.append("[")
                        .append(i + 1)
                        .append("] ")
                        .append(src != null ? "(" + src + ")" : "")
                        .append(seg.text())
                        .append("\n\n");
            }

            String prompt = String.format("""
                    You are a professional RAG assistant. Answer the user's question based on the following reference materials.
                    If the materials are insufficient, say so honestly.
                    
                    === Reference Materials ===
                    %s
                    === User Question ===
                    %s
                    === Answer ===
                    """, context.toString(), question);

            // ---- 5. 调用 DeepSeek（挂了 OI listener，自动出 LLM 子 span）----
            String answer = chatModel.chat(prompt);

            rootSpan.setAttribute("output.value", answer);
            return answer;

        } finally {
            rootSpan.end();
        }
    }
}
