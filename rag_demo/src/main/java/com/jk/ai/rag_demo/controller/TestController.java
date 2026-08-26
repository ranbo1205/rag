package com.jk.ai.rag_demo.controller;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.LambdaStreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final ChatModel deepseekChatModel;
    private final StreamingChatModel deepStreamingChatModel;
    public TestController(ChatModel deepseekChatModel, StreamingChatModel deepStreamingChatModel) {
        this.deepseekChatModel = deepseekChatModel;
        this.deepStreamingChatModel = deepStreamingChatModel;
    }


    /** 启动日志：确认 Bean 注入成功 */
    @PostConstruct
    public void init(){

        System.out.println("################################");
        System.out.println("  DeepSeek ChatModel: " + deepseekChatModel.getClass().getSimpleName());
        System.out.println("  DeepSeek StreamingChatModel: " + deepStreamingChatModel.getClass().getSimpleName());
        System.out.println("################################");

    }

    /** 健康检查 */
    @GetMapping("/health")
    public Map<String,String> health(){
        Map<String,String> map = new HashMap<>();
        map.put("status","UP");
        map.put("chatModel", deepseekChatModel.getClass().getSimpleName());
        return map;
    }

    /**
     * 测试 DeepSeek 非流式对话
     * POST /api/test/chat
     * Body: {"message": "用一句话介绍你自己"}
     */
    @PostMapping("/chat")
    public Map<String, Object> chat (@RequestBody Map<String, String> body){

        String message = body.getOrDefault("message", "你好，请自我介绍一下吧");

        long timestamp = System.currentTimeMillis();

        // 1.15.0 标准写法：ChatRequest + UserMessage
        ChatRequest request = ChatRequest.builder().messages(UserMessage.from(message)).build();
        ChatResponse response = deepseekChatModel.chat(request);
        String answer = response.aiMessage().text();

        long elapsed = System.currentTimeMillis() - timestamp;

        Map<String, Object> result = new HashMap<>();
        result.put("success",true);
        result.put("provider","deepseek");
        result.put("question",message);
        result.put("answer",answer);
        result.put("elapsedMs",elapsed);

        return result;
    }

    /* ---------- 3. 流式 Chat（真正 SSE 逐 token） ---------- */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream (@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "用三句话介绍一下深圳");
        long timestamp = System.currentTimeMillis();

        // 用 Flux.create 把回调式流式 API 桥接为 Reactor Flux
       return Flux.create(sink -> {

           deepStreamingChatModel.chat(message,
                   LambdaStreamingResponseHandler.onPartialResponseAndError(
                           sink::next,  // onPartialResponse: 每个 token 推给 sink
                           sink::error  // onError: 错误推给 sink
                   ));
       });


    }


}
