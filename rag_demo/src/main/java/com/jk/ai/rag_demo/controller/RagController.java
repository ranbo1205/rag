package com.jk.ai.rag_demo.controller;

import com.jk.ai.rag_demo.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }


    @PostMapping("/ingest")
    public Map<String, Object> ingest(@RequestBody Map<String, String> body) {

        String content = body.get("content");
        String source = body.getOrDefault("source", "manual");
        int chunks = ragService.ingest(content, source);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("chunks", chunks);
        result.put("source", source);

        return result;

    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        String answer = ragService.chat(question);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("question", question);
        result.put("answer", answer);
        result.put("provider", "deepseek + pgvector-hnsw");
        return result;


    }

}
