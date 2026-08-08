📚 IT-Wiki-RAG

面向技术团队的智能知识库问答系统 | Java 后端工程师的 RAG 落地实践

!https://img.shields.io/badge/Java-17-blue.svg
!https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg
!https://img.shields.io/badge/LangChain4j-0.27.0-orange.svg
!https://img.shields.io/badge/License-MIT-yellow.svg

📖 简介

技术团队的 Wiki、技术方案、运维手册、API 文档往往散落在 Confluence、GitLab、本地硬盘中，检索效率低、新人培训成本高。本项目是一个完全基于 Java 技术栈的 RAG（检索增强生成）落地实践，实现了从文档解析、向量化入库到智能问答的全流程，核心解决以下问题：
• 大模型「幻觉」问题：强制基于企业内部文档回答，拒绝编造

• 技术文档检索难：支持代码块保护、Markdown 层级识别，精准召回

• Java 工程师学习门槛低：无需切换 Python 技术栈，所有核心逻辑均用 Spring Boot 实现

本项目既可以作为企业内部知识库直接使用，也可以作为 Java 程序员学习 RAG 落地的实战项目，更是深圳地区企业 RAG 副业接单的现成脚手架。

✨ 核心特性

• 多格式文档支持：完美解析 .md/.docx/.pdf/.java/.yml 等技术团队常用格式

• 智能文本切分：

• 自动识别代码块（```包裹内容），避免切分断裂
- 保留 Markdown 层级结构，自动提取章节元数据
- 重叠切分策略，避免上下文边界信息丢失
- 企业级入库能力：
    - 文件 MD5 幂等校验，避免重复入库
    - 异步批量入库，支持大文件上传不超时
    - 任务进度实时查询
- 生产级问答体验：
    - 流式输出（打字机效果），响应速度快
    - 引用溯源：回答自动标注来源文档和章节
    - 元数据过滤：支持按分类、版本筛选检索范围
- 灵活部署方案：支持本地 Ollama 部署（低成本）或云端 DeepSeek/通义千问 API（高稳定）

🛠️ 技术栈

领域 技术选型 说明

核心框架 Spring Boot 3.2.1 + Spring WebFlux 支持流式输出，非阻塞 IO

AI 框架 LangChain4j 0.27.0 Java 生态最成熟的 LLM 应用框架

向量数据库 PostgreSQL + pgvector 开源免费，支持向量相似度检索

嵌入模型 bge-m3（Ollama）/ text-embedding-v2（阿里云） 中文语义理解效果好

生成模型 DeepSeek-Chat / Qwen2-7B 性价比高，适合企业场景

文档解析 Apache POI + PDFBox + Tika 支持多格式文档解析

构建工具 Maven 3.8+ Java 项目标准构建工具

📋 环境要求

- JDK 17+（Spring Boot 3.x 强制要求）
- Maven 3.8+
- PostgreSQL 14+（已安装 pgvector 扩展）
- Ollama（可选，本地部署嵌入模型用）
- Docker（可选，一键部署环境用）

🚀 快速开始

方式一：本地手动部署

1. 克隆仓库

git clone https://github.com/your-username/it-wiki-rag.git
cd it-wiki-rag


2. 准备数据库

-- 创建数据库
CREATE DATABASE rag_db;
-- 切换到目标库
\c rag_db
-- 启用 pgvector 扩展（必须执行）
CREATE EXTENSION IF NOT EXISTS vector;


3. 配置环境变量（重要：不要硬编码密钥）

# 大模型 API Key（使用 DeepSeek 时配置）
export DEEPSEEK_API_KEY="sk-your-deepseek-api-key"
# 可选：阿里云百炼 API Key（使用云端嵌入模型时配置）
export DASHSCOPE_API_KEY="sk-your-dashscope-api-key"
# 数据库密码
export DB_PASSWORD="your-postgres-password"


4. 启动服务

mvn clean install
mvn spring-boot:run

服务默认启动在 http://localhost:8080

5. 验证服务

curl http://localhost:8080/actuator/health
# 预期返回 {"status":"UP"}


方式二：Docker 一键部署（推荐）

# 启动 PostgreSQL + pgvector + Ollama + 应用
docker-compose up -d
# 等待服务启动完成后，拉取嵌入模型
docker exec -it rag-ollama ollama pull bge-m3


数据入库示例

同步入库（适合 <10MB 的小文件）

curl -X POST http://localhost:8080/api/rag/ingest/sync \
-F "file=@/path/to/你的Java编码规范.md" \
-F "category=tech-wiki"


异步入库（适合大文件/批量文件）

# 提交异步任务
curl -X POST http://localhost:8080/api/rag/ingest/async \
-F "file=@/path/to/大型运维手册.pdf" \
-F "category=ops-wiki"
# 查询任务进度
curl http://localhost:8080/api/rag/ingest/progress/{返回的taskId}


问答示例

curl -X POST http://localhost:8080/api/rag/chat/stream \
-H "Content-Type: application/json" \
-d '{"question":"Java中如何优雅处理NullPointerException？"}'

返回示例（流式输出）：

Java中处理NullPointerException的核心原则是「避免空指针优先于捕获异常」，推荐以下几种优雅处理方式：
1. 使用Objects.nonNull()/Objects.requireNonNull()进行前置校验
2. 优先使用Optional类封装可能为null的对象
3. 使用@NotNull/@Nullable注解明确参数约束
   [来源: 《Java编码规范.md》 - 第3章 异常处理]


📁 目录结构


it-wiki-rag
├── src/main/java/com/example/rag
│   ├── config          # 配置类（LangChain4j、异步线程池等）
│   ├── controller      # 接口层（入库、问答、进度查询）
│   ├── service         # 业务逻辑层
│   │   ├── DocumentParsingService.java    # 文档解析
│   │   ├── TextSplittingService.java      # 智能切分
│   │   ├── EmbeddingIngestionService.java # 向量化入库
│   │   ├── RetrievalService.java          # 知识检索
│   │   └── ChatService.java               # 问答编排
│   ├── repository      # 数据访问层（PGVector操作）
│   ├── dto             # 传输对象（请求/响应/进度）
│   ├── util            # 工具类（文件哈希、MD5计算等）
│   └── model           # 实体类（DocumentChunk等）
├── src/main/resources
│   ├── application.yml # 核心配置文件
│   └── data/wiki       # 初始化文档目录
├── docker-compose.yml  # Docker一键部署配置
├── pom.xml             # Maven依赖配置
└── README.md           # 本文件


🔄 核心流程

文档入库流程（Ingestion）

graph TD
A[上传文档] --> B[解析文档:提取文本+元数据]
B --> C[智能切分:保护代码块+重叠切分]
C --> D[MD5幂等校验]
D --> E[批量向量化:Embedding模型]
E --> F[批量写入PGVector]
F --> G[返回入库结果]


问答流程（RAG）

graph TD
A[用户输入问题] --> B[问题向量化]
B --> C[向量相似度检索:TopK相关片段]
C --> D[Prompt拼接:上下文+问题+约束规则]
D --> E[大模型生成答案]
E --> F[流式返回+SSE推送]
F --> G[前端展示:带引用溯源的答案]


⚙️ 核心配置说明

src/main/resources/application.yml 核心配置项：
langchain4j:
embedding-model:
# 本地Ollama配置（默认）
ollama:
base-url: http://localhost:11434
model-name: bge-m3
# 云端配置（可选，取消注释启用）
# dashscope:
#   api-key: ${DASHSCOPE_API_KEY}
#   model-name: text-embedding-v2
chat-model:
# DeepSeek配置（默认）
open-ai:
base-url: https://api.deepseek.com/v1
api-key: ${DEEPSEEK_API_KEY}
model-name: deepseek-chat
temperature: 0.1 # 降低随机性，保证技术回答准确性

rag:
ingestion:
init-dir: ./data/wiki # 启动时自动加载的文档目录
chunk-size: 800       # 文本切分块大小（中文建议500-1000）
overlap: 150          # 切分重叠区大小


📚 API 文档

接口路径 方法 说明

/actuator/health GET 服务健康检查

/api/rag/ingest/sync POST 同步文档入库

/api/rag/ingest/async POST 异步文档入库

/api/rag/ingest/progress/{taskId} GET 查询异步任务进度

/api/rag/chat/stream POST 流式问答接口

/api/rag/chat POST 非流式问答接口（不推荐）

❓ 常见问题

1. 启动时报错「could not resolve dependency vector」

原因：PostgreSQL 未安装 pgvector 扩展。
解决：执行 CREATE EXTENSION IF NOT EXISTS vector; 即可。

2. 代码块被切断，回答不准确

解决：调整 application.yml 中的 rag.ingestion.chunk-size 参数，建议设置为代码块平均长度的 1.5 倍，同时确保切分逻辑中代码块标记识别正确。

3. 检索不到相关内容

排查步骤：
1. 检查 embedding 模型是否与入库时一致
2. 确认元数据过滤条件是否正确
3. 调大检索 TopK 数量（默认5，可调整为10）
4. 检查文档是否已成功入库（查询数据库 knowledge_vector 表）

4. 流式输出乱码

解决：前端需使用 EventSource 或支持 SSE 的客户端，确保响应头 Content-Type: text/event-stream;charset=UTF-8。

5. 大模型回答出现幻觉

解决：检查 Prompt 中的约束规则是否生效，确保检索到的上下文足够相关，可降低 temperature 参数至 0.05 以下。

🎯 学习价值

完成本项目后，你将系统掌握以下 RAG 核心能力：
- ✅ Spring Boot 3.x + LangChain4j 实战开发
- ✅ PGVector 向量数据库的 CRUD 与索引优化
- ✅ 文档解析、智能切分、Embedding 向量化全流程
- ✅ 流式接口开发、异步任务处理、企业级幂等设计
- ✅ Prompt 工程、RAG 效果优化、幻觉抑制方法

🛣️ Roadmap
混合检索：BM25 全文检索 + 向量检索融合

Rerank 重排序：提升检索结果准确率

多租户隔离：支持不同部门/团队数据隔离

企微/钉钉集成：直接在企业 IM 中使用问答

可视化管理后台：文档管理、检索测试、效果评估

模型微调：支持基于企业内部数据微调 Embedding 模型

🤝 贡献指南

欢迎提交 Issue 和 PR！
1. Fork 本仓库
2. 创建特性分支（git checkout -b feature/AmazingFeature）
3. 提交改动（git commit -m 'Add some AmazingFeature'）
4. 推送到分支（git push origin feature/AmazingFeature）
5. 打开 Pull Request

请确保代码通过 Maven 测试，遵循现有代码风格。

📄 License

本项目基于 MIT 协议开源，详情见 LICENSE 文件。

💡 适合人群：Java 后端工程师学习 RAG 落地、中小团队搭建内部知识库、深圳地区企业 RAG 副业接单
📍 副业场景：企业知识库、智能客服后端、政务文档问答、跨境电商知识库等

有问题欢迎提 Issue，或者加入交流群讨论~
```

这个 README 完全贴合你的 Java 技术栈和副业需求，放到 GitHub 上既专业又能体现项目价值。如果需要我补充 docker-compose.yml 的完整内容，或者帮你调整成更符合你个人风格的版本，随时告诉我~