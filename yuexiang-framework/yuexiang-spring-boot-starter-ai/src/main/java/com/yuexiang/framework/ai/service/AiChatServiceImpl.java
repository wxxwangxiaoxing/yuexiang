package com.yuexiang.framework.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired(required = false)
    private ChatModel chatModel;

    @Override
    public String chat(String message) {
        return chat(message, null);
    }

    @Override
    public String chat(String message, String systemPrompt) {
        if (chatModel == null) {
            log.warn("[AI服务未配置] 请配置 spring.ai.openai.api-key 或排除 OpenAiAutoConfiguration");
            return "AI服务未配置，请联系管理员";
        }
        try {
            ChatClient client = createChatClientBuilder()
                    .defaultSystem(systemPrompt)
                    .build();

            return client.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[AI聊天失败] message={}, error={}", message, e.getMessage(), e);
            throw new RuntimeException("AI聊天失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ChatClient.Builder createChatClientBuilder() {
        if (chatModel == null) {
            throw new IllegalStateException("AI服务未配置，请配置 spring.ai.openai.api-key");
        }
        return ChatClient.builder(chatModel);
    }
}
