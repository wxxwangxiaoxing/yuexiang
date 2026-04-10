package com.yuexiang.ai.service;

import com.yuexiang.ai.domain.entity.AiMessage;
import com.yuexiang.ai.domain.vo.AiMessageVO;

import java.util.List;

public interface AiMessageService {

    AiMessage saveUserMessage(String sessionId, String content);

    AiMessage saveAssistantMessage(String sessionId, String content, List<Long> recommendedShopIds);

    AiMessage saveAssistantMessage(String sessionId, String content, String messageType,
                                  List<Long> recommendedShopIds, String cardsData);

    AiMessage saveToolMessage(String sessionId, String toolName, String toolArgs, String toolResult);

    List<AiMessage> listMessages(String sessionId);

    List<AiMessage> listRecentMessages(String sessionId, int limit);

    List<AiMessageVO> listMessageVOs(String sessionId);

    AiMessageVO toMessageVO(AiMessage message);
}
