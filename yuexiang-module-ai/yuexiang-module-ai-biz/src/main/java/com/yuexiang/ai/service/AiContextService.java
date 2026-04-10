package com.yuexiang.ai.service;

import com.yuexiang.ai.domain.entity.AiMessage;
import com.yuexiang.ai.domain.entity.AiSession;

import java.util.List;

public interface AiContextService {

    List<AiMessage> loadRecentConversation(String sessionId, int maxMessages);

    String buildConversationContext(String sessionId, int maxMessages);

    String buildMergedSystemPrompt(AiSession session, List<AiMessage> recentMessages);

    void updateContextAfterReply(AiSession session, String userMessage, String assistantMessage,
                                 List<Long> recommendedShopIds);
}
