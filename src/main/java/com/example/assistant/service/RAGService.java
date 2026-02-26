package com.example.assistant.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * RAG (Retrieval-Augmented Generation) 服务
 * 
 * 演示文档向量化、检索增强生成
 * 
 * 已知问题：
 * 1. 使用内存存储，重启后数据丢失
 * 2. 大文档分块策略需要精细调优
 * 3. PDF解析对复杂格式支持有限
 */
@Slf4j
@Service
public class RAGService {

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;
    private ContentRetriever contentRetriever;

    /**
     * 初始化RAG组件
     * 
     * TODO: 支持从配置文件加载文档路径
     * TODO: 接入Redis/Milvus等持久化向量存储
     */
    @PostConstruct
    public void init() {
        log.info("初始化RAG服务...");
        
        // 使用本地量化Embedding模型（体积约80MB）
        this.embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        
        // 内存存储（仅用于演示）
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        
        // 内容检索器
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)  // 检索Top3相关片段
                .minScore(0.7)  // 相似度阈值
                .build();
        
        log.info("RAG服务初始化完成");
    }

    /**
     * 加载PDF文档到向量存储
     * 
     * 踩坑记录：
     * 默认分块策略对技术文档不友好，
     * 需要调整chunkSize和overlap
     * 
     * @param pdfPath PDF文件路径
     */
    public void loadPdfDocument(String pdfPath) {
        log.info("加载PDF文档: {}", pdfPath);
        
        try {
            Path path = Paths.get(pdfPath);
            
            // 解析PDF
            Document document = FileSystemDocumentLoader.loadDocument(
                    path,
                    new ApachePdfBoxDocumentParser()
            );
            
            // 自定义分块策略
            // chunkSize: 每个块的最大字符数
            // overlap: 相邻块的重叠字符数（保证语义连续性）
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
            List<TextSegment> segments = splitter.split(document);
            
            log.info("文档分块完成，共 {} 个片段", segments.size());
            
            // 向量化并存储
            for (TextSegment segment : segments) {
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
            }
            
            log.info("文档向量化完成，已存入向量库");
            
        } catch (Exception e) {
            log.error("加载PDF文档失败", e);
            throw new RuntimeException("文档加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 基于文档内容回答问题
     * 
     * 简单实现：先检索相关片段，然后拼接到Prompt
     * 生产环境建议使用LangChain4j的RAG模块
     * 
     * @param question 用户问题
     * @return 基于文档的回答
     */
    public String answerFromDocuments(String question) {
        log.info("RAG查询: {}", question);
        
        // 检索相关片段
        List<dev.langchain4j.rag.content.Content> relevantDocs = 
                contentRetriever.retrieve(dev.langchain4j.rag.query.Query.from(question));
        
        if (relevantDocs.isEmpty()) {
            return "抱歉，根据现有文档无法回答这个问题。";
        }
        
        // 构建增强Prompt
        StringBuilder context = new StringBuilder();
        context.append("基于以下文档内容回答问题：\n\n");
        for (int i = 0; i < relevantDocs.size(); i++) {
            context.append("[片段").append(i + 1).append("]\n");
            context.append(relevantDocs.get(i).textSegment().text());
            context.append("\n\n");
        }
        context.append("问题：").append(question);
        context.append("\n\n请基于以上文档内容回答，如果文档中没有相关信息，请明确说明。");
        
        return context.toString();
    }

    /**
     * 获取向量存储中的文档数量
     */
    public int getDocumentCount() {
        // InMemoryEmbeddingStore没有直接提供count方法
        // 生产环境使用外部存储时需要实现
        return -1;
    }

    /**
     * 清空向量存储
     */
    public void clearDocuments() {
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();
        log.info("向量存储已清空");
    }
}
