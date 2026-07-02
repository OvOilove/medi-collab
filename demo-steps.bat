@echo off
chcp 65001 >nul
title MediCollab 演示脚本 - 按步骤执行
set GW=http://localhost:8080

echo.
echo  ╔══════════════════════════════════════════════╗
echo  ║   MediCollab 演示脚本 - 10项要点逐条演示   ║
echo  ╚══════════════════════════════════════════════╝
echo.
echo  每步执行完后按任意键继续下一步
echo.

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第5点: 登录认证 (JWT + BCrypt)          │
echo └──────────────────────────────────────────┘
echo.
echo  请求: POST %GW%/api/user/login
echo  请求体: {"username":"admin","password":"123456"}
echo.
curl -s -X POST %GW%/api/user/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"
echo.
echo.
echo  ↑ 返回 JWT Token + 用户信息 ↑
echo  口播: 密码BCrypt加密,Token有效期7天,同时缓存到Redis
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第8点: API网关 - 无Token被拦截(401)     │
echo └──────────────────────────────────────────┘
echo.
echo  请求: GET %GW%/api/user/list  (不带Token)
echo.
curl -s -w "\nHTTP状态码: %%{http_code}\n" %GW%/api/user/list
echo.
echo  ↑ 返回 401 Unauthorized — JWT过滤器生效 ↑
echo  口播: Gateway全局过滤器拦截,白名单放行login/register
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第8点: API网关 - 带Token正常访问        │
echo └──────────────────────────────────────────┘
echo.
echo  请求: GET %GW%/api/user/list (带Token)
echo.
REM 先获取token
for /f "delims=" %%i in ('curl -s -X POST %GW%/api/user/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}" ^| python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])"') do set TOKEN=%%i
curl -s %GW%/api/user/list -H "Authorization: Bearer %TOKEN%"
echo.
echo  ↑ Gateway→lb://medi-user→返回用户列表 ↑
echo  口播: 路由转发 + JWT认证 + 请求头注入X-User-Id
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第1点: 接口文档 - 查看完整API列表       │
echo └──────────────────────────────────────────┘
echo.
echo  浏览器打开: http://localhost:8083/doc.html
echo  展示 Knife4j 接口文档页面
echo  口播: 系统共52个API,覆盖增删改查,支持在线调试
echo  请手动打开浏览器演示接口文档
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第6点: 注册中心 - Nacos Dashboard       │
echo └──────────────────────────────────────────┘
echo.
echo  浏览器打开: http://localhost:8848/nacos
echo  账号密码: nacos / nacos
echo  进入 服务管理→服务列表→选择 medicollab 命名空间
echo.
echo  当前注册的服务:
curl -s "http://127.0.0.1:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10&namespaceId=medicollab"
echo.
echo  口播: 5个服务已注册,展示健康检查+心跳
echo  请手动打开浏览器演示 Nacos Dashboard
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第7点: 远程调用 - 创建科室              │
echo └──────────────────────────────────────────┘
echo.
echo  请求: POST localhost:8082/api/appointment/dept
curl -s -X POST localhost:8082/api/appointment/dept -H "Content-Type: application/json" -d "{\"name\":\"心内科\",\"hospitalId\":1,\"hospitalName\":\"第一人民医院\",\"location\":\"3楼A区\"}"
echo.
echo  ↑ 科室创建成功 ↑
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第7点: 远程调用 - 创建排班              │
echo └──────────────────────────────────────────┘
echo.
curl -s -X POST localhost:8082/api/appointment/schedule -H "Content-Type: application/json" -d "{\"doctorId\":2,\"doctorName\":\"张医生\",\"departmentId\":1,\"departmentName\":\"心内科\",\"hospitalId\":1,\"hospitalName\":\"第一人民医院\",\"workDate\":\"2026-07-08\",\"startTime\":\"08:00\",\"endTime\":\"12:00\",\"maxPatients\":30}"
echo.
echo  ↑ 排班创建成功 ↑
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第7点: 远程调用 - 创建预约(Feign调用)   │
echo └──────────────────────────────────────────┘
echo.
echo  appointment服务通过Feign调用user服务校验患者
echo.
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"赵患者\",\"doctorId\":2,\"doctorName\":\"张医生\",\"departmentId\":1,\"departmentName\":\"心内科\",\"hospitalId\":1,\"hospitalName\":\"第一人民医院\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"09:00\"}"
echo.
echo  ↑ 预约成功 queueNumber=1, appointment→user Feign调用通过 ↑
echo  口播: OpenFeign声明式调用,服务发现lb://medi-user
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第2点: Redis - 科室热度排行(ZSet)       │
echo └──────────────────────────────────────────┘
echo.
echo  再创建2个预约增加热度...
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"赵患者\",\"doctorId\":2,\"doctorName\":\"张医生\",\"departmentId\":1,\"departmentName\":\"心内科\",\"hospitalId\":1,\"hospitalName\":\"第一人民医院\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"09:30\"}" >nul
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"赵患者\",\"doctorId\":2,\"doctorName\":\"张医生\",\"departmentId\":1,\"departmentName\":\"心内科\",\"hospitalId\":1,\"hospitalName\":\"第一人民医院\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"10:00\"}" >nul
echo.
echo  查看Redis热度排行:
curl -s localhost:8082/api/appointment/dept/hot?topN=5
echo.
echo  ↑ 心内科 score=3, 每次预约自动 ZINCRBY ↑
echo  口播: Redis ZSet热度排行 + SETNX分布式锁 + 用户缓存
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第9点: 配置中心 - 实时刷新配置          │
echo └──────────────────────────────────────────┘
echo.
echo  当前阈值: appointment.max-per-day = 100
echo  通过Nacos API修改为 3...
curl -s -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs" -d "dataId=medi-appointment.yaml&group=DEFAULT_GROUP&tenant=medicollab&content=appointment.max-per-day: 3%%0Aappointment.lock-timeout: 30" >nul
echo  配置已更新! (服务无需重启)
echo.
echo  尝试创建第4个预约 (阈值=3,前面已有3个):
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"赵患者\",\"doctorId\":2,\"doctorName\":\"张医生\",\"departmentId\":1,\"departmentName\":\"心内科\",\"hospitalId\":1,\"hospitalName\":\"第一人民医院\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"10:30\"}"
echo.
echo  ↑ 被拒绝: "该科室今日预约已满 (阈值: 3)" ↑
echo  口播: @RefreshScope实时生效,无需重启,Dashboard也可修改
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第1点: 接口文档补充 - 病历+转诊         │
echo └──────────────────────────────────────────┘
echo.
echo  创建电子病历 (含SHA-256审计哈希):
curl -s -X POST localhost:8083/api/record -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"赵患者\",\"doctorId\":2,\"doctorName\":\"张医生\",\"departmentName\":\"心内科\",\"hospitalName\":\"第一人民医院\",\"chiefComplaint\":\"胸闷3天\",\"diagnosis\":\"冠心病\",\"treatmentPlan\":\"住院治疗\"}"
echo.
echo  ↑ 病历创建成功, auditHash已生成 ↑
echo.
echo  提交转诊申请:
curl -s -X POST localhost:8084/api/referral -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"赵患者\",\"fromDoctorId\":4,\"fromDoctorName\":\"王医生\",\"fromDeptName\":\"全科\",\"fromHospitalName\":\"社区医院\",\"toDeptName\":\"心内科\",\"toHospitalName\":\"第一人民医院\",\"referralReason\":\"胸闷加重需转上级医院\"}"
echo.
echo  ↑ 转诊申请提交 status=PENDING ↑
echo  口播: 系统共52个API,覆盖用户/预约/病历/转诊4个服务
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第4点: Git 提交记录                     │
echo └──────────────────────────────────────────┘
echo.
echo  Git 提交历史:
git -C D:\T\medi-collab log --oneline
echo.
echo  远程仓库:
git -C D:\T\medi-collab remote -v
echo.
echo  口播: 2次提交已推送到远程,共81个文件
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第3点: 单元测试                         │
echo └──────────────────────────────────────────┘
echo.
echo  运行24个单元测试...
echo  命令: mvn test -Dtest="*MockTest"
echo.
D:\Reasonix\task\a\maven\bin\mvn.cmd -f D:\T\medi-collab\pom.xml test -pl medi-user,medi-appointment,medi-record,medi-referral -Dtest="*MockTest" 2>&1 | findstr "Tests run: BUILD"
echo.
echo  口播: 24个测试全部通过,JUnit5+Mockito框架
pause >nul

REM ==========================================
echo.
echo ┌──────────────────────────────────────────┐
echo │ 第10点: 开发难点总结                    │
echo └──────────────────────────────────────────┘
echo.
echo  重点展示以下文件:
echo  1. RedisConfig.java - LocalDateTime序列化
echo  2. R.java - 泛型类型推断okMsg方法
echo  3. UserServiceImpl - BCrypt密码加密
echo  4. JwtAuthFilter - Gateway全局JWT过滤器
echo  5. AppointmentServiceImpl - Feign调用+分布式锁+RefreshScope
echo.
echo  请手动打开 IDE 展示代码
pause >nul

echo.
echo ╔══════════════════════════════════════════════╗
echo ║          演示脚本执行完毕！                 ║
echo ║  10项要点全部覆盖，可以停止录制了           ║
echo ╚══════════════════════════════════════════════╝
pause
