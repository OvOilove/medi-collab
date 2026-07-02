@echo off
set GW=http://localhost:8080
title MediCollab Demo

echo   MediCollab Demo - Press Enter to step
echo.

echo Initializing Nacos...
curl -s -X POST "http://127.0.0.1:8848/nacos/v1/console/namespaces" -d "customNamespaceId=medicollab&namespaceName=medicollab" >nul 2>&1
curl -s -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs" -d "dataId=medi-appointment.yaml&group=DEFAULT_GROUP&tenant=medicollab&content=appointment.max-per-day: 100" >nul 2>&1
echo Done.

echo.
echo  第1步：登录认证（对应第5点）
echo  JWT令牌签发 + BCrypt密码加密，有效期7天，同时缓存到Redis
echo.
pause >nul
curl -s -X POST %GW%/api/user/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}" | python "%~dp0fmt.py"
echo.
echo  ^> 复制上面返回的 token 字段，后面用到

echo.
echo  第2步：API网关 - 不带Token（对应第8点）
echo  Spring Cloud Gateway全局JWT过滤器，无Token返回401
echo.
pause >nul
curl -s -w "\nHTTP: %%{http_code}" %GW%/api/user/list
echo.
echo  ^> 401 Unauthorized，JWT过滤器生效

echo.
echo  第3步：API网关 - 带Token（对应第8点）
echo  Gateway验证JWT，注入X-User-Id等请求头，lb负载均衡转发到medi-user
echo.
pause >nul
for /f %%i in ('python "%~dp0get_token.py"') do set TOKEN=%%i
curl -s %GW%/api/user/list -H "Authorization: Bearer %TOKEN%" | python "%~dp0fmt.py"
echo.
echo  ^> Gateway路由+JWT认证成功

echo.
echo  第4步：接口文档展示（对应第1点）
echo  打开IDE展示4个Controller：UserController(12) AppointmentController(14) RecordController(13) ReferralController(13)
echo  共52个API，每个方法有@Tag和@Operation注解
echo.
pause >nul

echo.
echo  第5步：Nacos注册中心（对应第6点）
echo  这里选了 Eureka 更方便的替代方案 Nacos，同时提供注册+配置两项功能
echo  浏览器打开 http://localhost:8848/nacos  账号nacos密码nacos
echo  左侧服务管理-服务列表-顶部选medicollab命名空间-5个服务
echo.
pause >nul
curl -s "http://127.0.0.1:8848/nacos/v1/ns/service/list?namespaceId=medicollab&pageNo=1&pageSize=10" | python "%~dp0fmt.py"

echo.
echo  第6.1步：远程调用 - 创建科室（对应第7点）
echo  appointment服务REST API增删改查
echo.
pause >nul
curl -s -X POST localhost:8082/api/appointment/dept -H "Content-Type: application/json" -d "{\"name\":\"Cardiology\",\"hospitalId\":1,\"hospitalName\":\"No1-Hospital\"}" | python "%~dp0fmt.py"
echo.
echo  ^> 科室创建成功

echo.
echo  第6.2步：远程调用 - 创建排班（对应第7点）
echo  医生排班管理接口
echo.
pause >nul
curl -s -X POST localhost:8082/api/appointment/schedule -H "Content-Type: application/json" -d "{\"doctorId\":2,\"doctorName\":\"Dr.Zhang\",\"departmentId\":1,\"departmentName\":\"Cardiology\",\"hospitalId\":1,\"hospitalName\":\"No1-Hospital\",\"workDate\":\"2026-07-08\",\"startTime\":\"08:00:00\",\"endTime\":\"12:00:00\",\"maxPatients\":30}" | python "%~dp0fmt.py"
echo.
echo  ^> 排班创建成功

echo.
echo  第6.3步：远程调用 - 创建预约（对应第7点）
echo  OpenFeign跨服务调用：appointment通过Feign调用user校验患者，Nacos服务发现+LoadBalancer
echo.
pause >nul
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"Zhao\",\"doctorId\":2,\"doctorName\":\"Dr.Zhang\",\"departmentId\":1,\"departmentName\":\"Cardiology\",\"hospitalId\":1,\"hospitalName\":\"No1-Hospital\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"09:00:00\"}" | python "%~dp0fmt.py"
echo.
echo  ^> 预约创建成功，Feign调用user服务校验患者通过

echo.
echo  第7步：Redis缓存（对应第2点）
echo  3种使用模式：1)ZSet热度排行 2)SETNX分布式锁 3)String用户缓存
echo  创建额外2个预约，展示热度变化
echo.
pause >nul
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"Zhao\",\"doctorId\":2,\"doctorName\":\"Dr.Zhang\",\"departmentId\":1,\"departmentName\":\"Cardiology\",\"hospitalId\":1,\"hospitalName\":\"No1-Hospital\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"09:30:00\"}" >nul
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"Zhao\",\"doctorId\":2,\"doctorName\":\"Dr.Zhang\",\"departmentId\":1,\"departmentName\":\"Cardiology\",\"hospitalId\":1,\"hospitalName\":\"No1-Hospital\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"10:00:00\"}" >nul
curl -s "localhost:8082/api/appointment/dept/hot?topN=5" | python "%~dp0fmt.py"
echo.
echo  ^> 热度排行通过Redis ZINCRBY实时更新

echo.
echo  第8步：Nacos配置中心实时刷新（对应第9点）
echo  @RefreshScope注解，max-per-day从100改为3，服务无需重启
echo  第4个预约应被新阈值拦截
echo.
pause >nul
curl -s -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs" -d "dataId=medi-appointment.yaml&group=DEFAULT_GROUP&tenant=medicollab&content=appointment.max-per-day: 3" >nul
echo 配置已更新: max-per-day = 100 改为 3（服务未重启）
echo 尝试第4个预约：
curl -s -X POST localhost:8082/api/appointment -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"Zhao\",\"doctorId\":2,\"doctorName\":\"Dr.Zhang\",\"departmentId\":1,\"departmentName\":\"Cardiology\",\"hospitalId\":1,\"hospitalName\":\"No1-Hospital\",\"scheduleId\":1,\"appointmentDate\":\"2026-07-08\",\"timeSlot\":\"10:30:00\"}" | python "%~dp0fmt.py"
echo.
echo  ^> 被拦截，新阈值(3)实时生效

echo.
echo  第9步：电子病历（SHA-256审计哈希）
echo  每份病历生成唯一哈希值，用于防篡改验证
echo.
pause >nul
curl -s -X POST localhost:8083/api/record -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"Zhao\",\"doctorId\":2,\"doctorName\":\"Dr.Zhang\",\"departmentName\":\"Cardiology\",\"hospitalName\":\"No1-Hospital\",\"chiefComplaint\":\"chest pain\",\"diagnosis\":\"CHD\",\"treatmentPlan\":\"admit\"}" | python "%~dp0fmt.py"
echo.
echo  ^> 病历创建成功，auditHash为SHA-256哈希值

echo.
echo  第10步：双向转诊（RabbitMQ异步通知）
echo  转诊申请提交，状态PENDING等待目标医院审批
echo.
pause >nul
curl -s -X POST localhost:8084/api/referral -H "Content-Type: application/json" -d "{\"patientId\":5,\"patientName\":\"Zhao\",\"fromDoctorId\":4,\"fromDoctorName\":\"Dr.Wang\",\"fromDeptName\":\"GP\",\"fromHospitalName\":\"Community\",\"toDeptName\":\"Cardiology\",\"toHospitalName\":\"No1-Hospital\",\"referralReason\":\"need higher level care\"}" | python "%~dp0fmt.py"
echo.
echo  ^> 转诊申请已提交，状态PENDING

echo.
echo  第11步：Git版本管理（对应第4点）
echo  项目已推送GitHub: https://github.com/OvOilove/medi-collab
echo.
pause >nul
git -C D:\T\medi-collab log --oneline

echo.
echo  第12步：单元测试（对应第3点）
echo  24个测试，JUnit5+Mockito，覆盖4个服务
echo.
pause >nul
call D:\Reasonix\task\a\maven\bin\mvn.cmd -f D:\T\medi-collab\pom.xml test -pl medi-user,medi-appointment,medi-record,medi-referral "-Dtest=*MockTest"

echo.
echo  第13步：开发难点展示（对应第10点）
echo    RedisConfig.java           - LocalDateTime序列化配置
echo    R.java                     - 泛型类型推断(okMsg方法)
echo    UserServiceImpl.java       - BCrypt加密+JWT签发
echo    JwtAuthFilter.java         - Gateway全局JWT过滤器
echo    AppointmentServiceImpl     - Feign+分布式锁+RefreshScope
echo.
pause

echo.
echo  演示完毕
pause
