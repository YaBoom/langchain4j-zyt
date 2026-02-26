package com.example.assistant.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI对话服务
 * 
 * 演示基础对话、多轮对话、声明式接口调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final ChatMemory chatMemory;

    /**
     * 基础单轮对话
     */
    public String chat(String message) {
        log.info("收到用户消息: {}", message);
        String response = chatLanguageModel.chat(message);
        log.info("AI响应: {}", response);
        return response;
    }

    /**
     * 多轮对话（带上下文记忆）
     * 
     * 注意：当前实现是单用户共享ChatMemory，
     * 生产环境需要按用户隔离
     */
    public String chatWithMemory(String message) {
        log.info("多轮对话 - 用户消息: {}", message);
        
        // 将用户消息加入记忆
        chatMemory.add(dev.langchain4j.data.message.UserMessage.userMessage(message));
        
        // 调用模型（自动包含记忆上下文）
        String response = chatLanguageModel.chat(chatMemory.messages());
        
        // 将AI响应加入记忆
        chatMemory.add(dev.langchain4j.data.message.AiMessage.aiMessage(response));
        
        return response;
    }

    /**
     * 声明式接口调用 - 代码审查助手
     * 
     * 这是LangChain4j最优雅的设计之一，
     * 只需要定义接口，框架自动实现
     */
    public String reviewCode(String code) {
        CodeReviewer reviewer = AiServices.create(CodeReviewer.class, chatLanguageModel);
        return reviewer.review(code);
    }

    /**
     * 声明式接口 - 技术顾问
     */
    public String askArchitect(String question) {
        TechnicalArchitect architect = AiServices.create(TechnicalArchitect.class, chatLanguageModel);
        return architect.answer(question);
    }

    /**
     * AI代码审查接口定义
     */
    public interface CodeReviewer {
        @SystemMessage("你是一位经验丰富的Java架构师，擅长代码审查。" +
                "请分析代码的潜在问题，包括但不限于：" +
                "1. 潜在的NPE风险" +
                "2. 线程安全问题" +
                "3. 性能瓶颈" +
                "4. 代码规范" +
                "请用中文回复，简洁明了。")
        @UserMessage("请审查以下代码：\n\n```java\n{{code}}\n```")
        String review(@V("code") String code);
    }

    /**
     * 技术架构师接口定义
     */
    public interface TechnicalArchitect {
        @SystemMessage("你是一位资深技术架构师，拥有15年Java和企业级系统开发经验。" +
                "你擅长系统架构设计、技术选型、性能优化。" +
                "回答要专业、全面，同时考虑实际落地成本。")
        @UserMessage("{{question}}")
        String answer(@V("question") String question);
    }
}
