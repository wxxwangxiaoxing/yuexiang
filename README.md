# Yuexiang Backend

悦享生活平台后端工程。该项目采用 **Java 17 + Spring Boot 3**，以 **Maven 多模块单体** 方式组织代码，覆盖认证、用户中心、探店笔记、商户搜索、优惠券/秒杀、AI 探店等核心业务。

本 README 面向新加入项目的开发同学，帮助你快速了解：

- 项目是什么
- 模块如何划分
- 本地需要哪些依赖
- 如何完成配置并启动项目
- 主要业务能力落在哪些模块

---

## 1. 项目定位

Yuexiang Backend 是悦享生活平台的后端服务端工程，当前采用 **按业务域拆分的单体多模块架构**，而不是微服务架构。

项目特点：

- 以 `yuexiang-server` 作为统一启动入口
- 以 `yuexiang-framework` 提供公共基础设施能力
- 以 `yuexiang-module-*` 承载各业务域实现
- 业务模块普遍采用 `api` / `biz` 分层，便于复用和边界隔离

---

## 2. 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 17 |
| 应用框架 | Spring Boot 3.2.x |
| ORM | MyBatis-Plus |
| 数据库 | MySQL 8.x |
| 缓存 | Redis / Redisson |
| 搜索 | Elasticsearch 8.x |
| 认证 | JWT |
| API 文档 | Knife4j / OpenAPI |
| AI 能力 | Spring AI + DeepSeek / OpenAI / Qwen |
| 消息队列 | RocketMQ（默认关闭） |
| 对象存储 | MinIO / 阿里云 OSS |

---

## 3. 业务能力概览

当前仓库覆盖的核心业务能力如下：

### 3.1 系统与认证

- 图形验证码
- 短信验证码发送
- 短信登录 / 密码登录
- Token 刷新
- 单端登出 / 全会话吊销
- 上传等通用系统能力

### 3.2 用户中心

- 我的优惠券
- 我的订单
- 我的笔记
- 我的收藏
- 浏览足迹
- 签到与签到日历
- AI 探店记录

### 3.3 探店笔记

- 笔记发布
- 草稿保存与草稿更新
- 点赞、评论、关注等互动能力
- AI 辅助文案能力

### 3.4 商户能力

- 商户详情
- 附近商户
- 商户搜索
- 商户类型
- 收藏、点评等互动能力

### 3.5 优惠券与秒杀

- 普通优惠券能力
- 店铺券能力
- 秒杀场次与券列表
- 秒杀下单
- 秒杀订单结果查询

### 3.6 AI 探店

- AI 普通对话
- AI 流式对话（SSE）
- 会话详情查询
- 会话删除

产品/API 设计文档见 [docs/api/](docs/api/)。

---

## 4. 仓库结构

```text
.
├─ pom.xml                         # 根聚合工程
├─ yuexiang-dependencies/          # 统一依赖与版本管理
├─ yuexiang-framework/             # 公共能力与自定义 starter
├─ yuexiang-module-system/         # 系统模块
├─ yuexiang-module-user/           # 用户模块
├─ yuexiang-module-blog/           # 笔记模块
├─ yuexiang-module-shop/           # 商户模块
├─ yuexiang-module-voucher/        # 优惠券 / 秒杀模块
├─ yuexiang-module-ai/             # AI 探店模块
├─ yuexiang-server/                # 应用启动模块
└─ docs/                           # 业务与接口设计文档
```

---

## 5. 模块职责说明

### 5.1 根工程

- [pom.xml](pom.xml)
  - 聚合所有子模块
  - 统一定义 Java 版本与 Maven 编译参数

### 5.2 依赖管理层

- [yuexiang-dependencies/](yuexiang-dependencies/)
  - 管理 Spring Boot、MyBatis-Plus、Redis、Spring AI、Elasticsearch 等版本
  - 作为所有业务模块与 framework 模块的父依赖基础

### 5.3 公共框架层

- [yuexiang-framework/](yuexiang-framework/)
  - 承载项目级公共能力

其中主要包括：

- `yuexiang-common`：统一返回体、分页对象、异常、枚举
- `yuexiang-spring-boot-starter-web`：Web 通用配置、全局异常处理
- `yuexiang-spring-boot-starter-security`：登录态上下文、JWT 鉴权相关能力
- `yuexiang-spring-boot-starter-mybatis`：MyBatis-Plus 配置、基础实体能力
- `yuexiang-spring-boot-starter-redis`：Redis 集成
- `yuexiang-spring-boot-starter-swagger`：OpenAPI / Knife4j 文档能力
- `yuexiang-spring-boot-starter-storage`：对象存储能力
- `yuexiang-spring-boot-starter-log`：日志能力
- `yuexiang-spring-boot-starter-mq`：RocketMQ 集成
- `yuexiang-spring-boot-starter-ai`：AI 模型统一接入能力

### 5.4 业务模块层

每个业务域通常按照如下方式拆分：

- `xxx-module-*-api`
  - 放公共 DTO / VO / 领域对象 / 对外接口定义
- `xxx-module-*-biz`
  - 放 Controller / Service / Mapper / XML / 业务实现

当前主要业务模块职责如下：

| 模块 | 说明 |
| --- | --- |
| [yuexiang-module-system/](yuexiang-module-system/) | 认证、验证码、上传等系统级能力 |
| [yuexiang-module-user/](yuexiang-module-user/) | 个人中心、签到、消息、浏览足迹等用户域能力 |
| [yuexiang-module-blog/](yuexiang-module-blog/) | 探店笔记发布、互动、AI 辅助内容 |
| [yuexiang-module-shop/](yuexiang-module-shop/) | 商户详情、附近商户、搜索、收藏、点评 |
| [yuexiang-module-voucher/](yuexiang-module-voucher/) | 优惠券、秒杀、订单相关能力 |
| [yuexiang-module-ai/](yuexiang-module-ai/) | AI 探店对话与会话管理 |

### 5.5 启动模块

- [yuexiang-server/](yuexiang-server/)
  - 聚合所有业务模块与 framework starter
  - 项目统一启动入口位于 [YueXiangApplication.java](yuexiang-server/src/main/java/com/yuexiang/server/YueXiangApplication.java)

---

## 6. 本地开发环境要求

启动项目前，建议确保本地具备以下环境：

| 组件 | 建议版本 | 是否必须 |
| --- | --- | --- |
| JDK | 17 | 是 |
| Maven | 3.9+ | 是 |
| MySQL | 8.x | 是 |
| Redis | 6.x / 7.x | 是 |
| Elasticsearch | 8.x | 否（商户搜索相关功能需要） |
| RocketMQ | 与项目配置匹配 | 否（默认关闭） |

如需调试 AI 能力，还需准备至少一种模型提供方的 API Key。

---

## 7. 配置说明

主配置文件：

- [yuexiang-server/src/main/resources/application.yml](yuexiang-server/src/main/resources/application.yml)

默认配置项主要包括：

- 服务端口：`8080`
- MySQL 数据源
- Redis 连接
- Elasticsearch 连接
- Knife4j 文档开关
- RocketMQ 开关
- `yuexiang.business.*` 业务规则配置
- `yuexiang.ai.*` AI 模型配置

### 7.1 建议通过环境变量覆盖的配置

```bash
MYSQL_URL=jdbc:mysql://localhost:3306/yuexiang?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
MYSQL_USERNAME=root
MYSQL_PASSWORD=123456
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_DATABASE=0
ES_URIS=http://localhost:9200
DEEPSEEK_API_KEY=your-api-key
OPENAI_API_KEY=your-api-key
QWEN_API_KEY=your-api-key
```

### 7.2 配置建议

- 不要在仓库中提交真实数据库密码、Redis 密码或 AI 密钥
- 本地开发优先使用环境变量覆盖敏感配置
- AI 模型配置建议仅保留占位值，真实密钥通过运行环境注入

---

## 8. 本地启动说明

### 8.1 编译项目

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

### 8.2 启动应用

方式一：直接使用 Maven 启动

```bash
mvn -pl yuexiang-server -am spring-boot:run
```

方式二：打包后运行

```bash
java -jar yuexiang-server/target/yuexiang-server-1.0.0-SNAPSHOT.jar
```

### 8.3 验证服务是否启动成功

默认访问地址：

- `http://localhost:8080`

若 Knife4j 保持开启，通常可通过以下地址查看接口文档：

- `http://localhost:8080/doc.html`

---

## 9. 开发约定

### 9.1 接口返回结构

项目统一使用 `CommonResult<T>` 作为接口响应包装。

### 9.2 登录态获取方式

业务代码通常通过 `UserContext` 获取当前用户：

- 匿名可访问接口：优先判断 `UserContext.get()` 是否为空
- 必须登录接口：直接使用 `UserContext.getUserId()`

### 9.3 持久层约定

- 使用 MyBatis-Plus + Mapper XML
- 通用实体通常继承 `BaseEntity`
- 默认支持逻辑删除与乐观锁字段

### 9.4 典型请求链路

```text
Controller -> Service -> Mapper -> MySQL / Redis / Elasticsearch / AI Provider
```

### 9.5 代码阅读建议

如果你是第一次接手该项目，建议按如下顺序阅读：

1. [pom.xml](pom.xml)
2. [yuexiang-server/](yuexiang-server/)
3. [yuexiang-framework/](yuexiang-framework/)
4. 当前负责的业务模块，例如：
   - [yuexiang-module-user/](yuexiang-module-user/)
   - [yuexiang-module-voucher/](yuexiang-module-voucher/)
   - [yuexiang-module-shop/](yuexiang-module-shop/)
5. 对应的 `docs/api` 文档

---

## 10. 业务文档索引

建议优先阅读以下文档以建立产品上下文：

- [docs/api/user/个人中心.md](docs/api/user/%E4%B8%AA%E4%BA%BA%E4%B8%AD%E5%BF%83.md)
- [docs/api/blog/发布笔记页.md](docs/api/blog/%E5%8F%91%E5%B8%83%E7%AC%94%E8%AE%B0%E9%A1%B5.md)
- [docs/api/优惠券模块API接口文档.md](docs/api/%E4%BC%98%E6%83%A0%E5%88%B8%E6%A8%A1%E5%9D%97API%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3.md)
- [docs/api/ai/AI 探店助手.md](docs/api/ai/AI%20%E6%8E%A2%E5%BA%97%E5%8A%A9%E6%89%8B.md)

---

## 11. 适合新同学的快速理解路径

如果你的目标是尽快开始开发，建议先建立下面这套认知：

- **先看启动入口**：确认项目如何聚合模块
- **再看 framework**：确认统一异常、认证、数据库等横切能力
- **再看目标业务模块**：理解 Controller / Service / Mapper / XML 的实现方式
- **最后看 docs/api**：把代码实现与产品/API 设计对应起来

这会比直接从单个 Controller 开始阅读更高效。

---

## 12. 当前架构说明

当前项目本质上是：

> **按业务域拆分的多模块单体后端系统**

它的优势在于：

- 模块边界已经较清晰
- 公共能力沉淀在 framework 层
- 启动与部署仍保持单体应用的简单性
- 后续如果需要做服务拆分，可以基于现有模块边界继续演进

---

## 13. 补充说明

- 本仓库包含一定数量的产品/API 文档，建议开发前先阅读对应业务文档
- 搜索、AI、MQ 等能力可能依赖额外基础设施，不是所有功能都能在最小依赖下完整运行
- 如果只是进行普通后端接口开发，通常具备 MySQL + Redis 即可完成大部分工作
