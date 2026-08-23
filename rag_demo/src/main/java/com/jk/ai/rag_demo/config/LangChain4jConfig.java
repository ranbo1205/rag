package com.jk.ai.rag_demo.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

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
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName) {

        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey).
                modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)  //调试期开启，方便看请求
                .build();

    }

    /**
     * DeepSeek Streaming Chat Model（流式）
     */
    @Bean
    public StreamingChatModel deepSeekStreamingChatModel(
            @Value("${langchain4j.open-ai.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.open-ai.chat-model.api-key}")  String apiKey,
            @Value("${langchain4j.open-ai.chat-model.model-name}") String modelName) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
