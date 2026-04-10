package com.yuexiang.framework.ai.service;

import org.springframework.ai.chat.client.ChatClient;

public interface AiChatService {
    
    String chat(String message);
    
    String chat(String message, String systemPrompt);

    ChatClient.Builder createChatClientBuilder();
}
