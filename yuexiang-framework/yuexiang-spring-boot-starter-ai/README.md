# Spring AI Starter

## 功能说明

本模块提供了Spring AI的集成，支持OpenAI、DeepSeek、Qwen等多种AI模型。

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.yuexiang</groupId>
    <artifactId>yuexiang-spring-boot-starter-ai</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置API密钥

在`application.yml`或`application.properties`中添加以下配置：

```yaml
# OpenAI配置
spring:
  ai:
    openai:
      api-key: your-openai-api-key
      base-url: https://api.openai.com
      chat:
        options:
          model: gpt-3.5-turbo
          temperature: 0.7

# 悦享AI配置
yuexiang:
  ai:
    primary: OPENAI
    fallback:
      enabled: true
```

### 3. 使用AI服务

```java
@Service
@RequiredArgsConstructor
public class MyService {
    
    private final AiChatService aiChatService;
    
    public String chat(String message) {
        return aiChatService.chat(message);
    }
    
    public String chatWithSystemPrompt(String message, String systemPrompt) {
        return aiChatService.chat(message, systemPrompt);
    }
}
```

## 支持的AI模型

### OpenAI

```yaml
spring.ai.openai.api-key=your-api-key
spring.ai.openai.base-url=https://api.openai.com
spring.ai.openai.chat.options.model=gpt-3.5-turbo
```

### DeepSeek

```yaml
spring.ai.openai.api-key=your-deepseek-api-key
spring.ai.openai.base-url=https://api.deepseek.com
spring.ai.openai.chat.options.model=deepseek-chat
```

### Qwen（通义千问）

```yaml
spring.ai.openai.api-key=your-qwen-api-key
spring.ai.openai.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1
spring.ai.openai.chat.options.model=qwen-turbo
```

## 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `yuexiang.ai.primary` | 主要使用的AI模型提供商 | OPENAI |
| `yuexiang.ai.fallback.enabled` | 是否启用降级策略 | true |

## 注意事项

1. 请确保API密钥的安全性，不要将密钥提交到代码仓库
2. 建议使用环境变量或配置中心管理API密钥
3. 不同的AI模型可能有不同的计费方式，请注意控制使用量

## 示例项目

查看`yuexiang-module-ai`模块了解更多使用示例。
