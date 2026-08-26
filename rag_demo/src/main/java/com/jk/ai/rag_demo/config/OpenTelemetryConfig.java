package com.jk.ai.rag_demo.config;


import com.arize.instrumentation.langchain4j.LangChain4jInstrumentor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenTelemetryConfig {


    // ★ 关键改动：注入 OpenTelemetry 参数，强制 Spring 先创建 openTelemetry Bean
    @Bean
    public LangChain4jInstrumentor langChain4jInstrumentor(OpenTelemetry openTelemetry) {
        // 此时 GlobalOpenTelemetry 已经被 openTelemetry() 设置好了
        // instrument() 内部 get() 直接拿到我们的 SDK，不会重复 set
        LangChain4jInstrumentor instrumentor = LangChain4jInstrumentor.instrument();
        System.out.println("✅ LangChain4j OpenInference instrumentation registered");
        return instrumentor;
    }

    @Bean
    public OpenTelemetry openTelemetry() {

        Resource resource = Resource.getDefault().toBuilder()
                .put("service.name","rag-backend")
                .put("openinference.project.name", "rag-test")
                .build();

        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .setTimeout(Duration.ofSeconds(10))
                .build();


        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).setScheduleDelay(Duration.ofSeconds(1)).build())
                .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .buildAndRegisterGlobal();


        // 测试 span（验证 gRPC 连通性，确认没问题后可以删掉）
        /*var testTracer = sdk.getTracer("test");
        var testSpan = testTracer.spanBuilder("startup-test").startSpan();
        testSpan.setAttribute("test", "hello");
        testSpan.end();

        try {
            sdk.getSdkTracerProvider().forceFlush()
                    .join(5, java.util.concurrent.TimeUnit.SECONDS);
            System.out.println("🧪 Force flush done - check Phoenix at http://localhost:6006");
        } catch (Exception e) {
            System.err.println("🧪 Flush FAILED: " + e.getMessage());
        }
*/

        return sdk;
    }

}
