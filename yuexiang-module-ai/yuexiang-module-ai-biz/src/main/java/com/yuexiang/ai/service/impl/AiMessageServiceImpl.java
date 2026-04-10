package com.yuexiang.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.ai.domain.entity.AiMessage;
import com.yuexiang.ai.domain.vo.AiMessageVO;
import com.yuexiang.ai.mapper.AiMessageMapper;
import com.yuexiang.ai.service.AiMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiMessageServiceImpl implements AiMessageService {

    private final AiMessageMapper aiMessageMapper;

    @Override
    public AiMessage saveUserMessage(String sessionId, String content) {
        AiMessage userMessage = new AiMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setMessageType("text");
        userMessage.setShopIds("[]");
        userMessage.setTokenUsage(0);
        userMessage.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(userMessage);
        return userMessage;
    }

    @Override
    public AiMessage saveAssistantMessage(String sessionId, String content, List<Long> recommendedShopIds) {
        return saveAssistantMessage(sessionId, content, "text", recommendedShopIds, null);
    }

    @Override
    public AiMessage saveAssistantMessage(String sessionId, String content, String messageType,
                                          List<Long> recommendedShopIds, String cardsData) {
        AiMessage assistantMessage = new AiMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(content);
        assistantMessage.setMessageType(messageType);
        assistantMessage.setShopIds(listToJson(recommendedShopIds));
        assistantMessage.setCardsData(cardsData);
        assistantMessage.setTokenUsage(0);
        assistantMessage.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(assistantMessage);
        return assistantMessage;
    }

    @Override
    public AiMessage saveToolMessage(String sessionId, String toolName, String toolArgs, String toolResult) {
        AiMessage toolMessage = new AiMessage();
        toolMessage.setSessionId(sessionId);
        toolMessage.setRole("tool");
        toolMessage.setMessageType("tool");
        toolMessage.setToolName(toolName);
        toolMessage.setToolArgs(toolArgs);
        toolMessage.setToolResult(toolResult);
        toolMessage.setContent(toolResult);
        toolMessage.setShopIds("[]");
        toolMessage.setTokenUsage(0);
        toolMessage.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(toolMessage);
        return toolMessage;
    }

    @Override
    public List<AiMessage> listMessages(String sessionId) {
        return aiMessageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getSessionId, sessionId)
                .orderByAsc(AiMessage::getCreateTime));
    }

    @Override
    public List<AiMessage> listRecentMessages(String sessionId, int limit) {
        return aiMessageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getSessionId, sessionId)
                .orderByDesc(AiMessage::getCreateTime)
                .last("LIMIT " + Math.max(limit, 1)));
    }

    @Override
    public List<AiMessageVO> listMessageVOs(String sessionId) {
        return listMessages(sessionId).stream().map(this::toMessageVO).toList();
    }

    @Override
    public AiMessageVO toMessageVO(AiMessage message) {
        return AiMessageVO.builder()
                .messageId(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .cardsData(message.getCardsData())
                .toolName(message.getToolName())
                .toolArgs(message.getToolArgs())
                .toolResult(message.getToolResult())
                .finishReason(message.getFinishReason())
                .tokenUsage(message.getTokenUsage())
                .errorCode(message.getErrorCode())
                .shopIds(message.getShopIds())
                .createTime(toTimestamp(message.getCreateTime()))
                .build();
    }

    private String listToJson(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ids.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    private Long toTimestamp(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
