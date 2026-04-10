# 悦享生活平台 - Code Wiki

## 目录
1. [项目概述](#项目概述)
2. [技术架构](#技术架构)
3. [项目结构](#项目结构)
4. [核心模块详解](#核心模块详解)
5. [关键类与函数](#关键类与函数)
6. [依赖关系](#依赖关系)
7. [项目运行方式](#项目运行方式)
8. [开发规范](#开发规范)

---

## 项目概述

### 项目简介
悦享生活平台是一个本地生活/美食探店类应用的后端系统，采用Java 17 + Spring Boot 3构建，为用户提供探店笔记、商户搜索、优惠券/秒杀、AI探店等核心功能。

### 核心特性
- 多模块单体架构，按业务域清晰划分
- 完善的认证体系（JWT + 短信/密码登录）
- 探店笔记发布与互动功能
- 商户详情、附近商户、搜索功能
- 优惠券与秒杀能力
- AI探店助手（支持DeepSeek/OpenAI/Qwen）
- 用户签到积分体系

---

## 技术架构

### 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言/运行时 | Java 17 |
| 应用框架 | Spring Boot 3.2.x |
| ORM | MyBatis-Plus |
| 数据库 | MySQL 8.x |
| 缓存 | Redis / Redisson |
| 搜索 | Elasticsearch 8.x |
| 认证 | JWT |
| API文档 | Knife4j / OpenAPI |
| AI能力 | Spring AI + DeepSeek / OpenAI / Qwen |
| 消息队列 | RocketMQ（默认关闭） |
| 对象存储 | MinIO / 阿里云 OSS |

### 架构设计
项目采用**按业务域拆分的多模块单体架构**，具有以下优势：
- 模块边界清晰，便于维护和扩展
- 公共能力沉淀在framework层
- 启动与部署保持单体应用的简单性
- 便于后续向微服务架构演进

---

## 项目结构

### 整体目录结构

```text
/workspace/
├── yuexiang-dependencies/          # 统一依赖与版本管理
├── yuexiang-framework/             # 公共能力与自定义starter
├── yuexiang-module-system/         # 系统模块（认证、上传等）
├── yuexiang-module-user/           # 用户模块（个人中心、签到等）
├── yuexiang-module-blog/           # 探店笔记模块
├── yuexiang-module-shop/           # 商户模块
├── yuexiang-module-voucher/        # 优惠券/秒杀模块
├── yuexiang-module-ai/             # AI探店模块
├── yuexiang-server/                # 应用启动模块
├── yuexiang-uniapp/                # 前端项目（UniApp）
└── docs/                            # 业务与接口设计文档
```

### 模块分层原则
每个业务模块通常按照如下方式拆分：
- `xxx-module-*-api`：放置公共DTO/VO/领域对象/对外接口定义
- `xxx-module-*-biz`：放置Controller/Service/Mapper/XML/业务实现

---

## 核心模块详解

### 1. yuexiang-dependencies
**职责**：统一依赖版本管理
- 管理Spring Boot、MyBatis-Plus、Redis、Spring AI、Elasticsearch等版本
- 作为所有业务模块与framework模块的父依赖基础

### 2. yuexiang-framework
**职责**：承载项目级公共能力，提供自定义Spring Boot Starter

#### 子模块说明：

| 模块 | 职责 |
| --- | --- |
| yuexiang-common | 统一返回体、分页对象、异常、枚举 |
| yuexiang-spring-boot-starter-web | Web通用配置、全局异常处理 |
| yuexiang-spring-boot-starter-security | 登录态上下文、JWT鉴权能力 |
| yuexiang-spring-boot-starter-mybatis | MyBatis-Plus配置、基础实体能力 |
| yuexiang-spring-boot-starter-redis | Redis集成 |
| yuexiang-spring-boot-starter-swagger | OpenAPI/Knife4j文档能力 |
| yuexiang-spring-boot-starter-storage | 对象存储能力 |
| yuexiang-spring-boot-starter-log | 日志能力 |
| yuexiang-spring-boot-starter-mq | RocketMQ集成 |
| yuexiang-spring-boot-starter-ai | AI模型统一接入能力 |

### 3. yuexiang-module-system
**职责**：认证、验证码、上传等系统级能力

#### 核心功能：
- 图形验证码生成与校验
- 短信验证码发送
- 短信登录/密码登录
- Token刷新
- 单端登出/全会话吊销
- 文件上传

#### 关键控制器：
- [AuthController](file:///workspace/yuexiang-module-system/yuexiang-module-system-biz/src/main/java/com/yuexiang/system/controller/AuthController.java)：认证相关接口

### 4. yuexiang-module-user
**职责**：个人中心、签到、消息、浏览足迹等用户域能力

#### 核心功能：
- 用户个人资料管理
- 签到与签到日历
- 积分体系
- 用户消息
- 浏览历史记录
- 收藏管理

#### 关键控制器：
- [UserCenterController](file:///workspace/yuexiang-module-user/yuexiang-module-user-biz/src/main/java/com/yuexiang/user/controller/UserCenterController.java)：个人中心接口
- [SignController](file:///workspace/yuexiang-module-user/yuexiang-module-user-biz/src/main/java/com/yuexiang/user/controller/SignController.java)：签到相关接口

### 5. yuexiang-module-blog
**职责**：探店笔记发布、互动、AI辅助内容

#### 核心功能：
- 笔记发布与编辑
- 草稿保存
- 点赞、评论、关注
- AI辅助文案生成
- 笔记摘要

#### 关键控制器：
- [BlogController](file:///workspace/yuexiang-module-blog/yuexiang-module-blog-biz/src/main/java/com/yuexiang/blog/controller/BlogController.java)：笔记相关接口
- [BlogPublishController](file:///workspace/yuexiang-module-blog/yuexiang-module-blog-biz/src/main/java/com/yuexiang/blog/controller/BlogPublishController.java)：笔记发布接口
- [BlogLikeController](file:///workspace/yuexiang-module-blog/yuexiang-module-blog-biz/src/main/java/com/yuexiang/blog/controller/BlogLikeController.java)：点赞相关接口

### 6. yuexiang-module-shop
**职责**：商户详情、附近商户、搜索、收藏、点评

#### 核心功能：
- 商户详情查询
- 附近商户查询
- 商户搜索（基于Elasticsearch）
- 商户类型分类
- 商户收藏
- 商户点评

#### 关键控制器：
- [ShopController](file:///workspace/yuexiang-module-shop/yuexiang-module-shop-biz/src/main/java/com/yuexiang/shop/controller/ShopController.java)：商户详情接口
- [ShopSearchController](file:///workspace/yuexiang-module-shop/yuexiang-module-shop-biz/src/main/java/com/yuexiang/shop/controller/ShopSearchController.java)：商户搜索接口

### 7. yuexiang-module-voucher
**职责**：优惠券、秒杀、订单相关能力

#### 核心功能：
- 普通优惠券管理
- 店铺优惠券管理
- 秒杀场次管理
- 秒杀下单
- 秒杀订单查询
- 订单超时处理

#### 关键控制器：
- [SeckillController](file:///workspace/yuexiang-module-voucher/yuexiang-module-voucher-biz/src/main/java/com/yuexiang/voucher/controller/SeckillController.java)：秒杀相关接口
- [VoucherController](file:///workspace/yuexiang-module-voucher/yuexiang-module-voucher-biz/src/main/java/com/yuexiang/voucher/controller/VoucherController.java)：优惠券相关接口

### 8. yuexiang-module-ai
**职责**：AI探店对话与会话管理

#### 核心功能：
- AI普通对话
- AI流式对话（SSE）
- 会话管理
- 热门问题
- 商户推荐

#### 关键控制器：
- [AiShopController](file:///workspace/yuexiang-module-ai/yuexiang-module-ai-biz/src/main/java/com/yuexiang/ai/controller/AiShopController.java)：AI相关接口

### 9. yuexiang-server
**职责**：聚合所有业务模块与framework starter，提供统一启动入口

#### 启动类：
- [YueXiangApplication](file:///workspace/yuexiang-server/src/main/java/com/yuexiang/server/YueXiangApplication.java)：应用主启动类

---

## 关键类与函数

### 公共类

#### CommonResult&lt;T&gt;
**位置**：[yuexiang-framework/yuexiang-common/src/main/java/com/yuexiang/common/pojo/CommonResult.java](file:///workspace/yuexiang-framework/yuexiang-common/src/main/java/com/yuexiang/common/pojo/CommonResult.java)

**职责**：统一API响应包装类

**关键方法**：
- `success(T data)`：成功响应，返回数据
- `success()`：成功响应，无数据
- `error(ResultCodeEnum errorCode)`：错误响应，使用预定义错误码
- `error(int code, String msg)`：错误响应，自定义错误码和消息

#### ResultCodeEnum
**位置**：[yuexiang-framework/yuexiang-common/src/main/java/com/yuexiang/common/enums/ResultCodeEnum.java](file:///workspace/yuexiang-framework/yuexiang-common/src/main/java/com/yuexiang/common/enums/ResultCodeEnum.java)

**职责**：统一错误码枚举

**主要错误码**：
- `SUCCESS(200, "成功")`
- `BAD_REQUEST(400, "请求参数错误")`
- `UNAUTHORIZED(401, "未登录")`
- `USER_NOT_FOUND(1001, "用户不存在")`
- `POINTS_INSUFFICIENT(2002, "积分不足")`

#### UserContext
**位置**：[yuexiang-framework/yuexiang-spring-boot-starter-security/src/main/java/com/yuexiang/framework/security/core/UserContext.java](file:///workspace/yuexiang-framework/yuexiang-spring-boot-starter-security/src/main/java/com/yuexiang/framework/security/core/UserContext.java)

**职责**：当前登录用户上下文管理

**关键方法**：
- `get()`：获取当前登录用户
- `getUserId()`：获取当前登录用户ID（必须登录）

### 认证模块

#### AuthController
**位置**：[yuexiang-module-system/yuexiang-module-system-biz/src/main/java/com/yuexiang/system/controller/AuthController.java](file:///workspace/yuexiang-module-system/yuexiang-module-system-biz/src/main/java/com/yuexiang/system/controller/AuthController.java)

**关键接口**：
- `GET /api/auth/captcha`：获取图形验证码
- `POST /api/auth/sms-code`：发送短信验证码
- `POST /api/auth/login/sms`：短信登录
- `POST /api/auth/login/password`：密码登录
- `POST /api/auth/token/refresh`：刷新Token
- `POST /api/auth/logout`：登出
- `DELETE /api/auth/sessions`：吊销所有会话

### 秒杀模块

#### SeckillController
**位置**：[yuexiang-module-voucher/yuexiang-module-voucher-biz/src/main/java/com/yuexiang/voucher/controller/SeckillController.java](file:///workspace/yuexiang-module-voucher/yuexiang-module-voucher-biz/src/main/java/com/yuexiang/voucher/controller/SeckillController.java)

**关键接口**：
- `GET /api/seckill/time`：获取服务器时间
- `GET /api/seckill/sessions`：查询秒杀场次列表
- `GET /api/seckill/session/{sessionId}/vouchers`：查询场次秒杀券列表
- `GET /api/seckill/voucher/{voucherId}`：查询秒杀券详情
- `POST /api/seckill/order`：秒杀下单
- `GET /api/seckill/order/{orderId}`：查询秒杀订单结果

### AI模块

#### AiShopController
**位置**：[yuexiang-module-ai/yuexiang-module-ai-biz/src/main/java/com/yuexiang/ai/controller/AiShopController.java](file:///workspace/yuexiang-module-ai/yuexiang-module-ai-biz/src/main/java/com/yuexiang/ai/controller/AiShopController.java)

**关键接口**：
- `POST /api/ai/chat`：AI对话
- `POST /api/ai/chat/stream`：AI流式对话（SSE）
- `GET /api/ai/session/{sessionId}`：获取会话详情
- `DELETE /api/ai/session/{sessionId}`：删除会话
- `GET /api/ai/chat/sessions`：获取会话列表
- `GET /api/ai/chat/hot-questions`：获取热门问题

---

## 依赖关系

### 模块依赖图

```
yuexiang-server (启动模块)
├── yuexiang-framework/* (所有starter)
├── yuexiang-module-system
├── yuexiang-module-user
├── yuexiang-module-blog
├── yuexiang-module-shop
├── yuexiang-module-voucher
└── yuexiang-module-ai

yuexiang-module-*-biz (业务实现)
└── yuexiang-module-*-api (API定义)

yuexiang-framework/* (所有starter)
└── yuexiang-common
```

### 核心技术依赖

- Spring Boot 3.2.x
- MyBatis-Plus
- Spring Data Redis
- Spring Data Elasticsearch
- Spring AI
- Knife4j
- Redisson
- RocketMQ（可选）

---

## 项目运行方式

### 环境要求

| 组件 | 建议版本 | 是否必须 |
| --- | --- | --- |
| JDK | 17 | 是 |
| Maven | 3.9+ | 是 |
| MySQL | 8.x | 是 |
| Redis | 6.x / 7.x | 是 |
| Elasticsearch | 8.x | 否 |
| RocketMQ | 与项目配置匹配 | 否 |

### 配置说明

主配置文件：[yuexiang-server/src/main/resources/application.yml](file:///workspace/yuexiang-server/src/main/resources/application.yml)

**关键配置项**：
- 服务端口：8080
- MySQL数据源配置
- Redis连接配置
- Elasticsearch连接配置
- AI模型配置（DeepSeek/OpenAI/Qwen）

**建议通过环境变量覆盖的配置**：
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

### 编译与启动

**1. 编译项目**
```bash
mvn clean package -DskipTests
```

**2. 启动应用**

方式一：使用Maven启动
```bash
mvn -pl yuexiang-server -am spring-boot:run
```

方式二：打包后运行
```bash
java -jar yuexiang-server/target/yuexiang-server-1.0.0-SNAPSHOT.jar
```

### 验证服务

- 服务访问地址：http://localhost:8080
- API文档地址：http://localhost:8080/doc.html

---

## 开发规范

### 接口返回结构
所有API接口统一使用 `CommonResult&lt;T&gt;` 作为响应包装。

### 登录态获取方式
- 匿名可访问接口：优先判断 `UserContext.get()` 是否为空
- 必须登录接口：直接使用 `UserContext.getUserId()`

### 持久层约定
- 使用 MyBatis-Plus + Mapper XML
- 通用实体通常继承 `BaseEntity`
- 默认支持逻辑删除与乐观锁字段

### 典型请求链路
```
Controller -> Service -> Mapper -> MySQL / Redis / Elasticsearch / AI Provider
```

### 代码阅读建议
1. 查看根 [pom.xml](file:///workspace/pom.xml)
2. 查看 [yuexiang-server/](file:///workspace/yuexiang-server/) 启动模块
3. 查看 [yuexiang-framework/](file:///workspace/yuexiang-framework/) 框架层
4. 查看目标业务模块
5. 查看 [docs/api/](file:///workspace/docs/api/) 业务文档

---

## 总结

悦享生活平台是一个设计良好的多模块单体应用，具有以下特点：

1. **架构清晰**：按业务域划分模块，边界明确
2. **技术先进**：采用Java 17 + Spring Boot 3等现代技术栈
3. **功能完整**：覆盖探店、商户、优惠券、AI等核心业务
4. **扩展性强**：模块化设计便于后续功能扩展和架构演进
5. **文档完善**：提供了丰富的API文档和业务设计文档

对于新开发者，建议按照"代码阅读建议"的顺序逐步了解项目。
