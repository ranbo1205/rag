@echo off
chcp 65001 >nul
echo ========================================
echo   RAG PostgreSQL + pgvector 一键启动
echo ========================================
echo.

REM 检查 Docker 是否运行
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker 未运行！请先启动 Docker Desktop。
    pause
    exit /b 1
)

REM 进入脚本所在目录
cd /d "%~dp0"

REM 启动容器
echo [INFO] 正在启动 PostgreSQL + pgvector...
docker compose up -d

REM 等待服务就绪
echo [INFO] 等待数据库初始化（约 5-10 秒）...
timeout /t 8 /nobreak >nul

REM 验证
echo.
echo [INFO] 验证容器状态：
docker compose ps

echo.
echo [INFO] 验证 pgvector 扩展：
docker exec rag-postgres psql -U postgres -d rag_db -c "\dx" 2>nul || echo "   容器还在初始化中，请稍后手动执行 docker exec rag-postgres psql -U postgres -d rag_db -c 'CREATE EXTENSION IF NOT EXISTS vector;'"

echo.
echo ========================================
echo   连接信息：
echo   主机: localhost:5432
echo   数据库: rag_db
echo   用户名: postgres
echo   密码: postgres
echo ========================================
echo.
pause