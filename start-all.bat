@echo off
chcp 65001 >nul
title MediCollab 演示 - 启动全部服务

echo ==========================================
echo   MediCollab 启动脚本
echo ==========================================
echo.

echo [1/7] 检�?Redis...
redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo   Redis 未启动！请先启动 Redis
    pause
    exit /b 1
)
echo   Redis OK

echo [2/7] 启动 Nacos (:8848)...
start "Nacos" /MIN D:\Reasonix\task\a\nacos-server\bin\startup.cmd -m standalone
echo   等待启动 (15�?...
timeout /t 15 /nobreak >nul

echo [3/7] 创建 Nacos 命名空间和配�?..
curl -s -X POST "http://127.0.0.1:8848/nacos/v1/console/namespaces" -d "customNamespaceId=medicollab&namespaceName=medicollab&namespaceDesc=medicollab" >nul 2>&1
curl -s -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs" -d "dataId=medi-appointment.yaml&group=DEFAULT_GROUP&tenant=medicollab&content=appointment.max-per-day: 100%%0Aappointment.lock-timeout: 30" >nul 2>&1
echo   OK

echo [4/7] 启动 medi-user (:8081)...
start "medi-user" /MIN D:\Reasonix\task\a\maven\bin\mvn.cmd -f D:\T\medi-collab\medi-user\pom.xml spring-boot:run -Dspring-boot.run.profiles=demo -q
timeout /t 10 /nobreak >nul

echo [5/7] 启动 medi-appointment (:8082)...
start "medi-appointment" /MIN D:\Reasonix\task\a\maven\bin\mvn.cmd -f D:\T\medi-collab\medi-appointment\pom.xml spring-boot:run -Dspring-boot.run.profiles=demo -q
timeout /t 6 /nobreak >nul

echo [6/7] 启动 medi-record (:8083) + medi-referral (:8084)...
start "medi-record" /MIN D:\Reasonix\task\a\maven\bin\mvn.cmd -f D:\T\medi-collab\medi-record\pom.xml spring-boot:run -Dspring-boot.run.profiles=demo -q
start "medi-referral" /MIN D:\Reasonix\task\a\maven\bin\mvn.cmd -f D:\T\medi-collab\medi-referral\pom.xml spring-boot:run -Dspring-boot.run.profiles=demo -q

echo [7/7] 启动 medi-gateway (:8080)...
start "medi-gateway" /MIN D:\Reasonix\task\a\maven\bin\mvn.cmd -f D:\T\medi-collab\medi-gateway\pom.xml spring-boot:run -q
timeout /t 12 /nobreak >nul

echo.
echo ==========================================
echo   全部启动完毕�?echo   Gateway : http://localhost:8080
echo   Nacos   : http://localhost:8848/nacos
echo   Knife4j : http://localhost:8083/doc.html
echo ==========================================
echo.
echo 按任意键验证服务注册...
pause >nul
curl -s "http://127.0.0.1:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10&namespaceId=medicollab"
echo.
echo 如果显示 "count":5 则就绪，可以开始演示！
pause
