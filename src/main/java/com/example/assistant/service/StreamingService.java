package com.example.assistant.service;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;

/**
 * 流式输出服务
 * 
 * 演示SSE (Server-Sent Events) 实时推送AI响应
 * 
 * 踩坑记录：
 * 1. Spring Boot需要配置异步超时：spring.mvc.async.request-timeout=300000
 * 2. SseEmitter需要正确处理complete和error
 * 3. 客户端断开连接时要及时清理资源
 */
@Slf4j
@Service
public class StreamingService {

    @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String modelName;

    /**
     * 创建流式对话
     * 
     * @param message 用户消息
     * @return SseEmitter 用于SSE推送
     */
    public SseEmitter streamChat(String message) {
        // 设置超时5分钟
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 配置流式模型
        StreamingChatLanguageModel streamingModel = OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(120))
                .build();
        
        // 异步执行流式调用
        new Thread(() -> {
            try {
                streamingModel.chat(message, new StreamingChatResponseHandler() {
                    
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        try {
                            // 推送片段到客户端
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(partialResponse));
                        } catch (IOException e) {
                            log.error("SSE发送失败", e);
                            emitter.completeWithError(e);
                        }
                    }
                    
                    @Override
                    public void onCompleteResponse(ChatResponse response) {
                        try {
                            // 发送完成标记
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data("[DONE]"));
                            emitter.complete();
                            log.info("流式响应完成");
                        } catch (IOException e) {
                            log.error("发送完成标记失败", e);
                            emitter.completeWithError(e);
                        }
                    }
                    
                    @Override
                    public void onError(Throwable error) {
                        log.error("流式调用失败", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(error.getMessage()));
                        } catch (IOException e) {
                            log.error("发送错误信息失败", e);
                        }
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                log.error("流式对话异常", e);
                emitter.completeWithError(e);
            }
        }).start();
        
        // 处理连接关闭
        emitter.onCompletion(() -> log.info("SSE连接完成"));
        emitter.onTimeout(() -> log.warn("SSE连接超时"));
        emitter.onError((e) -> log.error("SSE连接错误", e));
        
        return emitter;
    }

    /**
     * 带上下文的流式对话（简化版）
     * 
     * TODO: 集成ChatMemory实现多轮流式对话
     */
    public SseEmitter streamChatWithContext(String message, String conversationId) {
        // 当前简化实现，仅做转发
        // 生产环境需要：
        // 1. 按conversationId获取历史记录
        // 2. 构建包含上下文的Prompt
        // 3. 更新对话历史
        return streamChat(message);
    }
}
