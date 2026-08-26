package com.jk.ai.rag_demo.controller;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/embedding")
public class EmbeddingTestController {

    private final EmbeddingModel embeddingModel;

    public EmbeddingTestController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /** 健康检查：确认 Bean 注入成功 */
    @GetMapping("/health")
    public Map<String,String> health(){
        Map<String,String> map = new HashMap<>();
        map.put("status","UP");
        map.put("embedding model", embeddingModel.getClass().getSimpleName());
        return map;
    }

    /** 测试向量化：传入文本，返回维度和前5个值 */
    @PostMapping("/test")
    public Map<String, Object> test(@RequestBody Map<String, String> body){

        String text = body.getOrDefault("text", "RAG是检索增强");

        long start = System.currentTimeMillis();
        Embedding embedding = embeddingModel.embed(text).content();
        long elapsed = System.currentTimeMillis() - start;

        float[] vector = embedding.vector();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("inputText", text);
        result.put("dimension", vector.length); // 关键：应该是 1024
        result.put("first5Values", new Float[]{vector[0],vector[1],vector[2],vector[3],vector[4]});
        result.put("elapsedTime", elapsed);
        return result;
    }
}
