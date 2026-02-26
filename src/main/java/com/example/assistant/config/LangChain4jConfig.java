package com.example.assistant.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 * 
 * TODO: 支持多模型切换配置
 * TODO: 添加连接池配置
 */
@Configuration
public class LangChain4jConfig {

    @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String modelName;

    @Value("${deepseek.temperature:0.7}")
    private Double temperature;

    @Value("${deepseek.timeout:60}")
    private Integer timeoutSeconds;

    /**
     * 配置ChatLanguageModel
     * 使用DeepSeek API（兼容OpenAI格式）
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * 配置ChatMemory（对话记忆）
     * 使用滑动窗口，保留最近20条消息
     * 
     * 注意：每个用户应该有独立的ChatMemory实例，
     * 生产环境建议使用Redis等外部存储
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(20);
    }
}
