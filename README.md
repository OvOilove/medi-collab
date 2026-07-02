# MediCollab - 智慧医疗协同平台

基于 Spring Boot 3 + Spring Cloud Alibaba 的分布式医疗信息化系统，面向医联体场景，实现跨机构医疗资源协同。

- Spring Boot 3.2.5
- Spring Cloud 2023.0.3
- JDK 17+
- MIT License

---

## 项目简介

针对"医联体"场景，解决三大痛点：
- **检查结果互认难**：跨机构报告安全共享 + SHA-256 审计哈希链防篡改
- **转诊靠电话**：标准化双向转诊流程 + RabbitMQ 异步通知
- **热门科室挤兑**：Redis ZSet 实时热度排行 + 配置中心动态调整阈值

## 技术栈

| 组件 | 选型 | 说明 |
|------|------|------|
| 基础框架 | Spring Boot 3.2.5 | 微服务基础 |
| 微服务治理 | Spring Cloud 2023.0.3 + Alibaba 2023.0.1.0 | 分布式全家桶 |
| 注册/配置中心 | Nacos 2.3.2 | 服务发现 + 动态配置 |
| API 网关 | Spring Cloud Gateway | 统一入口 + JWT 全局认证 |
| 远程调用 | OpenFeign + LoadBalancer | 声明式服务调用 |
| 认证授权 | Spring Security + JWT + BCrypt | 无状态认证 |
| 缓存 | Redis 7 | 用户缓存 + 热度排行 + 分布式锁 |
| ORM | MyBatis-Plus 3.5.7 | 简化 CRUD |
| 消息队列 | RabbitMQ | 转诊异步通知 |
| 接口文档 | Knife4j / SpringDoc OpenAPI 3.0 | 自动生成 + 在线调试 |
| 数据库 | MySQL 8.0 / H2 (Demo) | 生产 + 测试双模式 |

## 系统架构

项目采用微服务架构，共 5 个服务模块：

```
客户端请求 → API Gateway (:8080)
                │
                ├──→ medi-user (:8081)        用户与认证服务
                ├──→ medi-appointment (:8082)  预约挂号服务
                ├──→ medi-record (:8083)       电子病历服务
                └──→ medi-referral (:8084)     双向转诊服务
                        │
                        ├──→ Nacos (:8848)      注册中心 + 配置中心
                        ├──→ Redis (:6379)      缓存 + 分布式锁
                        ├──→ RabbitMQ (:5672)   异步消息通知
                        └──→ MySQL / H2         数据持久化
```

**服务间调用关系：**
- Gateway 统一接收客户端请求，JWT 全局认证后路由转发到各服务
- Appointment 通过 OpenFeign 调用 User 服务校验患者信息
- Referral 通过 OpenFeign 调用 User 服务获取医生信息
- Record 独立管理病历和检查报告，通过 Redis 共享缓存
- 所有服务启动时自动注册到 Nacos，通过 LoadBalancer 实现负载均衡

## 接口统计（52 个 API）

| 服务 | 接口数 | 典型功能 |
|------|--------|---------|
| medi-user | 12 | 注册、登录、Token刷新、用户CRUD、角色管理 |
| medi-appointment | 14 | 预约CRUD、科室管理、医生排班、热度排行、号源锁定 |
| medi-record | 13 | 病历CRUD、检查报告、跨机构分享、审计链校验 |
| medi-referral | 13 | 转诊申请、审批、拒绝、完成、撤回 |
| **合计** | **52** | |

接口文档地址：启动后访问 `http://localhost:8083/doc.html`（Knife4j Gateway 聚合）

## 快速启动

### 前置环境
- JDK 17+
- Redis 7
- Nacos 2.3.2（单机模式）
- MySQL 8.0（可选，Demo 模式用 H2 内存库替代）

### 1. 启动基础设施
```bash
# Redis
redis-server

# Nacos（单机模式）
nacos-server/bin/startup.cmd -m standalone
```

### 2. 初始化数据库（可选）
```bash
mysql -u root -p < sql/init.sql
```

### 3. 启动服务（Demo 模式，H2 + 免 MySQL）
```bash
mvn -f medi-user/pom.xml spring-boot:run -Dspring-boot.run.profiles=demo
mvn -f medi-appointment/pom.xml spring-boot:run -Dspring-boot.run.profiles=demo
mvn -f medi-record/pom.xml spring-boot:run -Dspring-boot.run.profiles=demo
mvn -f medi-referral/pom.xml spring-boot:run -Dspring-boot.run.profiles=demo
mvn -f medi-gateway/pom.xml spring-boot:run
```

### 4. 验证
```bash
# 登录获取 Token
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 查看 Nacos 服务注册
curl http://localhost:8848/nacos/v1/ns/service/list?namespaceId=medicollab
```

## 一键演示

项目提供了三个演示脚本（需要 curl 和 Python）：

```bash
# 1. 启动全部服务（Redis + Nacos + 5个微服务）
start-all.bat

# 2. 逐条演示 10 项系统要点（按任意键执行下一步）
demo-steps.bat

# 3. 停止全部服务
stop-all.bat
```

演示内容涵盖：登录认证、API 网关、注册中心、远程调用、Redis 缓存、配置刷新、接口文档、单元测试、Git 提交记录。

## API 调用示例

完整的就诊流程演示：

```bash
# 1. 患者登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"patient_zhao","password":"123456"}'

# 2. 创建预约（appointment 服务通过 Feign 调用 user 服务校验患者）
curl -X POST http://localhost:8082/api/appointment \
  -H "Content-Type: application/json" \
  -d '{"patientId":5,"patientName":"赵患者","doctorId":2,"doctorName":"张医生","departmentId":1,"departmentName":"心内科","hospitalName":"第一人民医院","scheduleId":1,"appointmentDate":"2026-07-08","timeSlot":"09:00"}'

# 3. 查看科室热度排行（Redis ZSet）
curl http://localhost:8082/api/appointment/dept/hot?topN=10

# 4. 医生接诊后创建电子病历（SHA-256 审计哈希）
curl -X POST http://localhost:8083/api/record \
  -H "Content-Type: application/json" \
  -d '{"patientId":5,"patientName":"赵患者","doctorId":2,"doctorName":"张医生","departmentName":"心内科","hospitalName":"第一人民医院","chiefComplaint":"胸闷3天","diagnosis":"冠心病","treatmentPlan":"住院治疗"}'

# 5. 开具检查报告（哈希链关联前一条报告）
curl -X POST http://localhost:8083/api/record/report \
  -H "Content-Type: application/json" \
  -d '{"patientId":5,"patientName":"赵患者","doctorId":2,"doctorName":"张医生","examType":"CT","examPart":"胸部","findings":"双肺纹理增多","conclusion":"未见明显异常","recordId":1}'

# 6. 验证审计链完整性
curl http://localhost:8083/api/record/audit/verify/5

# 7. 社区医院发起转诊（RabbitMQ 异步通知目标医院）
curl -X POST http://localhost:8084/api/referral \
  -H "Content-Type: application/json" \
  -d '{"patientId":5,"patientName":"赵患者","fromDoctorName":"王医生","fromDeptName":"全科","fromHospitalName":"社区医院","toDeptName":"心内科","toHospitalName":"第一人民医院","referralReason":"胸闷加重需转上级医院"}'
```

## 单元测试

```bash
mvn test -pl medi-user,medi-appointment,medi-record,medi-referral -Dtest="*MockTest"
```

24 个测试全部通过（JUnit 5 + Mockito）。

## 核心功能

| 功能 | 说明 |
|------|------|
| JWT 认证 | BCrypt 加密存储，Gateway 全局过滤器校验，白名单放行登录/注册 |
| 服务注册发现 | Nacos 自动注册，心跳检测，Dashboard 可视化 |
| 远程调用 | OpenFeign 声明式调用，LoadBalancer 负载均衡 |
| 配置中心 | @RefreshScope 注解，Nacos 修改配置后实时生效，无需重启 |
| API 网关 | 统一入口 :8080，JWT 校验 + 路由转发到各服务 |
| Redis 缓存 | 用户信息缓存、ZSet 科室热度排行、SETNX 号源分布式锁 |
| 审计哈希链 | 每份检查报告关联前一份的 SHA-256 哈希，形成防篡改链 |
| 异步消息 | RabbitMQ 交换机 + 队列，转诊申请异步通知目标医院 |

## 项目结构

```
medi-collab/
├── pom.xml                    # 父 POM（依赖版本管理）
├── medi-common/               # 公共模块（DTO、工具类、异常处理）
├── medi-user/                 # 用户与认证服务 (:8081)
├── medi-appointment/          # 预约挂号服务 (:8082)
├── medi-record/               # 电子病历服务 (:8083)
├── medi-referral/             # 双向转诊服务 (:8084)
├── medi-gateway/              # API 网关 (:8080)
├── sql/                       # 数据库初始化脚本
└── README.md
```

## 开发难点

1. **Lombok + JDK21 不兼容** — `TypeTag::UNKNOWN` 编译错误，Lombok 1.18.34 仍不稳定，编写批处理脚本批量移除 Lombok 注解，替换为显式 getter/setter/构造器
2. **H2 与 MySQL 语法差异** — 集成测试 `BadSqlGrammar` 失败，改用 Mockito 纯单元测试隔离数据库依赖
3. **LocalDateTime JSON 序列化** — Redis 和 Web 层均需显式注册 `JavaTimeModule`，配置自定义 ObjectMapper
4. **Nacos 测试环境干扰** — @SpringBootTest 启动时尝试连接 Nacos 导致超时，通过 `autoconfigure.exclude` 和测试 bootstrap.yml 环境隔离
5. **构造器注入丢失** — 移除 @RequiredArgsConstructor 后 final 字段未初始化，编写脚本自动生成显式构造器

## License

MIT License
