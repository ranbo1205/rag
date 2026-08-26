### Local setup


<br>1. 启动postgresql db 
<br>PS D:\workspace\github\rag\rag_demo\src\main\docker>docker compose up -d

<br>2. 启动 Phoenix（Docker）
用 Docker 一条命令启动 Phoenix（含 UI + OTLP collector）：

docker run -d --name phoenix `
  -p 6006:6006 `
-p 4317:4317 `
  -e PHOENIX_PORT=6006 `
arizephoenix/phoenix:latest
<br>



### 使用docker 启动 pgvector 容器(postgresql db + 安装vector 扩展)， 用作rag 的embedding 数据库
## 1. 启动postgresql db
<BR>PS D:\workspace\github\rag\rag_demo\src\main\docker>docker compose up -d

## 2. 查看 pgvector 容器名字

<BR>PS D:\workspace\github\rag\rag_demo\src\main\docker> docker ps

<BR>CONTAINER ID   IMAGE                                              COMMAND                   CREATED          STATUS                    PORTS                                         NAMES

<BR>14c511256a4d   pgvector/pgvector:0.8.6-pg16                       "docker-entrypoint.s…"   19 minutes ago   Up 19 minutes (healthy)   0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   rag-postgres

## 3.验证 pgvector 扩展是否生效
<BR>PS D:\workspace\github\rag\rag_demo\src\main\docker> docker exec rag-postgres psql -U postgres -d rag_db -c "\dx"
## 环境验证记录

### PostgreSQL + pgvector

| 组件 | 版本 | 状态 |
|------|------|------|
| PostgreSQL | 16 | ✅ 运行中 (端口 5432) |
| pgvector | 0.8.6 | ✅ 扩展已启用 |
| 向量索引 | ivfflat + hnsw | ✅ 可用 |

验证命令：
\`\`\`powershell
docker exec rag-postgres psql -U postgres -d rag_db -c "\dx"
\`\`\`

验证输出：
| Name | Version | Schema | Description |<br>
|------|---------|--------|-------------|<br>
| plpgsql | 1.0 | pg_catalog | PL/pgSQL procedural language |<br>
| vector | 0.8.6 | public | vector data type and ivfflat and hnsw access methods |<br>

### 测试deepseek

## 1. 测试 deepseek API call success (non-stream)
<BR> post:http://localhost:8080/api/test/chat
<BR> HEADER: Content-Type: application/json; charset=utf-8
<BR> Body:{"message":"用一句话介绍你自己"}

## 2. 测试 deepseek api call (stream)
<BR> post:http://localhost:8080/api/test/chat/stream
<BR> HEADER: Content-Type: application/json; charset=utf-8
<BR> Body:{"message":"用3句话介绍西湖"}