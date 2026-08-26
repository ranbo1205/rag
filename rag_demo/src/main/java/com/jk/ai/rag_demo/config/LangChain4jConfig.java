package com.jk.ai.rag_demo.config;

import com.arize.instrumentation.langchain4j.LangChain4jInstrumentor;
import com.arize.instrumentation.langchain4j.LangChain4jModelListener;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class LangChain4jConfig {

    /**
     * 仅配置 DeepSeek Chat Model（OpenAI 兼容协议）。
     * 暂不配置 EmbeddingModel，避免没有通义 Key 时启动失败。
     */
    @Bean
    public ChatModel chatLanguageModel(
            @Value("${langchain4j.open-ai.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.open-ai.chat-model.api-key}")  String apiKey,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName,
            LangChain4jInstrumentor instrumentor) {  //注入，不再自己new

        // ★ 创建 listener 并挂到 ChatModel
        LangChain4jModelListener oiListener = instrumentor.createModelListener();

        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey).
                modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)  //调试期开启，方便看请求
                .listeners(List.of(oiListener))  // 关键 写到 otel phoenix
                .build();

    }

    /**
     * DeepSeek Streaming Chat Model（流式）
     */
    @Bean
    public StreamingChatModel deepSeekStreamingChatModel(
            @Value("${langchain4j.open-ai.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.open-ai.chat-model.api-key}")  String apiKey,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName,
            LangChain4jInstrumentor instrumentor) { // ★ 注入同一个实例

        LangChain4jModelListener oiListener = instrumentor.createModelListener();

        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofSeconds(60))
                .listeners(List.of(oiListener))  // ★ 挂 listener
                .build();
    }


    /**
     * 通义千问 Embedding Model（OpenAI 兼容协议）
     * base-url 指向 DashScope 的兼容端点，和 DeepSeek 不同，所以单独配置
     */

    @Bean
    public EmbeddingModel qwenEmbeddingModel(
            @Value("${langchain4j.open-ai.embedding-model.base-url}") String baseUrl,
            @Value("${langchain4j.open-ai.embedding-model.api-key}") String apiKey,
            @Value("${langchain4j.open-ai.embedding-model.model-name}") String modelName) {

        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();

    }
}
