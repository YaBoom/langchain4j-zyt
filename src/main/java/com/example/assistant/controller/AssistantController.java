package com.example.assistant.controller;

import com.example.assistant.service.AiChatService;
import com.example.assistant.service.RAGService;
import com.example.assistant.service.StreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * AI助手REST API控制器
 * 
 * 提供以下端点：
 * - POST /api/chat          基础对话
 * - POST /api/chat/memory   多轮对话
 * - POST /api/chat/stream   流式对话（SSE）
 * - POST /api/review        代码审查
 * - POST /api/rag/load      加载文档
 * - POST /api/rag/ask       RAG问答
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssistantController {

    private final AiChatService aiChatService;
    private final RAGService ragService;
    private final StreamingService streamingService;

    /**
     * 基础单轮对话
     */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到基础对话请求: {}", message);
        
        String response = aiChatService.chat(message);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return result;
    }

    /**
     * 多轮对话（带上下文记忆）
     * 
     * 注意：当前是单用户共享记忆，生产环境需要按用户隔离
     */
    @PostMapping("/chat/memory")
    public Map<String, String> chatWithMemory(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("收到多轮对话请求: {}", message);
        
        String response = aiChatService.chatWithMemory(message);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return result;
    }

    /**
     * 流式对话（SSE）
     * 
     * Content-Type: text/event-stream
     * 
     * 客户端示例：
     * const eventSource = new EventSource('/api/chat/stream?message=你好');
     * eventSource.onmessage = (e) => console.log(e.data);
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String message) {
        log.info("收到流式对话请求: {}", message);
        return streamingService.streamChat(message);
    }

    /**
     * AI代码审查
     */
    @PostMapping("/review")
    public Map<String, String> reviewCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        log.info("收到代码审查请求");
        
        String review = aiChatService.reviewCode(code);
        
        Map<String, String> result = new HashMap<>();
        result.put("review", review);
        return result;
    }

    /**
     * 技术顾问咨询
     */
    @PostMapping("/architect")
    public Map<String, String> askArchitect(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("收到技术顾问请求: {}", question);
        
        String answer = aiChatService.askArchitect(question);
        
        Map<String, String> result = new HashMap<>();
        result.put("answer", answer);
        return result;
    }

    /**
     * 加载PDF文档到RAG知识库
     */
    @PostMapping("/rag/load")
    public Map<String, Object> loadDocument(@RequestBody Map<String, String> request) {
        String pdfPath = request.get("path");
        log.info("加载文档: {}", pdfPath);
        
        try {
            ragService.loadPdfDocument(pdfPath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "文档加载成功");
            return result;
        } catch (Exception e) {
            log.error("文档加载失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    /**
     * 基于文档问答（RAG）
     */
    @PostMapping("/rag/ask")
    public Map<String, String> askFromDocuments(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        log.info("RAG问答: {}", question);
        
        String context = ragService.answerFromDocuments(question);
        
        // 使用增强后的上下文进行对话
        String response = aiChatService.chat(context);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        return result;
    }

    /**
     * 清空RAG知识库
     */
    @PostMapping("/rag/clear")
    public Map<String, Object> clearDocuments() {
        log.info("清空知识库");
        
        ragService.clearDocuments();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "知识库已清空");
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "langchain4j-assistant");
        return result;
    }
}
