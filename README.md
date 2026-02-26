# LangChain4j AI Assistant

🤖 一个基于 LangChain4j + Spring Boot 的 Java AI 助手示例项目

> 这个项目记录了我72小时踩坑LangChain4j的实战经验，包含完整可运行的代码。

## 🚀 快速开始

### 前置要求

- JDK 17+
- Maven 3.8+
- DeepSeek API Key (或其他兼容OpenAI API的Key)

### 配置

1. 克隆项目
```bash
git clone https://github.com/YaBoom/langchain4j-zyt.git
cd langchain4j-zyt
```

2. 设置API Key
```bash
export DEEPSEEK_API_KEY=your_api_key_here
```

3. 运行
```bash
mvn spring-boot:run
```

## 📁 项目结构

```
src/main/java/com/example/assistant/
├── config/           # 配置类
├── controller/       # REST API控制器
├── service/          # 业务逻辑层
│   ├── AiChatService.java      # 基础对话服务
│   ├── RAGService.java         # RAG检索增强生成
│   └── StreamingService.java   # 流式输出服务
└── AssistantApplication.java   # 启动类
```

## ✨ 功能特性

- [x] **基础对话**：支持多轮对话，上下文记忆
- [x] **RAG问答**：文档向量化检索 + LLM生成
- [x] **流式输出**：SSE实时推送，打字机效果
- [x] **接口化调用**：声明式AiServices

## 📊 性能数据

基于本地测试（DeepSeek V3模型）：

| 场景 | 响应时间 |
|------|----------|
| 单次调用（热） | ~1.3s |
| 100次顺序调用 | ~128s |
| 100次并发调用 | ~14s |
| 服务内存占用 | ~450MB（含1000文档索引） |

## ⚠️ 已知问题

1. **Temperature参数限制**：LangChain4j对temperature做了0-1校验，但DeepSeek支持0-2，需要自定义HTTP客户端绕过
2. **文档分块质量**：默认分块策略对技术文档不够友好，需要自定义DocumentSplitter
3. **流式输出配置**：Spring Boot需要显式配置异步超时 `spring.mvc.async.request-timeout`

## 📝 TODO

- [ ] 接入更多模型（Claude、Gemini、国内大模型）
- [ ] 持久化向量存储（Redis、Milvus、Pinecone）
- [ ] 多模态支持（图片理解、生成）
- [ ] Function Calling工具调用
- [ ] Agent智能体编排
- [ ] 监控与可观测性（链路追踪、Token消耗统计）

## 🔗 相关链接

- [LangChain4j官方文档](https://docs.langchain4j.dev/)
- [GitHub - langchain4j/langchain4j](https://github.com/langchain4j/langchain4j)
- [DeepSeek API文档](https://api-docs.deepseek.com/)

## 📄 License

MIT License

---

*Created with ❤️ by a Java developer who refuses to switch to Python*
