package com.yuexiang.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.ai.domain.entity.AiMessage;
import com.yuexiang.ai.domain.entity.AiSession;
import com.yuexiang.ai.service.AiContextService;
import com.yuexiang.ai.service.AiMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiContextServiceImpl implements AiContextService {

    private static final String SYSTEM_PROMPT = """
            你是「悦享生活」APP 的 AI 探店助手。

            【核心能力】
            1. 根据用户的口味偏好、预算、位置、场景，推荐合适的商户
            2. 基于真实的探店笔记和评价数据提供推荐理由
            3. 查询并展示商户的优惠券信息
            4. 搜索相关探店笔记供用户参考
            5. 进行自然的多轮对话，主动追问细化需求

            【行为规范】
            1. 只推荐数据库中真实存在的商户，不编造
            2. 推荐理由必须基于真实笔记和评价数据
            3. 价格信息使用"人均XX元"格式
            4. 距离信息基于用户坐标或指定地点计算
            5. 每次推荐 2~5 家商户
            6. 回答简洁友好
            7. 主动询问是否需要查看优惠券、更多详情等
            8. 非探店相关问题，礼貌引导回探店话题

            【实体修正规则】
            当用户说"更便宜的""换个近一点的"时，保留已有实体并调整对应字段。

            【地点处理规则】
            当用户提到具体地名时，调用 search_shops 函数并传入 location 字段。
            """;

    private final AiMessageService aiMessageService;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiMessage> loadRecentConversation(String sessionId, int maxMessages) {
        List<AiMessage> messages = aiMessageService.listRecentMessages(sessionId, maxMessages);
        List<AiMessage> ordered = new ArrayList<>(messages);
        Collections.reverse(ordered);
        return ordered;
    }

    @Override
    public String buildConversationContext(String sessionId, int maxMessages) {
        List<AiMessage> messages = loadRecentConversation(sessionId, maxMessages);
        if (messages.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (AiMessage message : messages) {
            builder.append(message.getRole()).append(": ")
                    .append(message.getContent() == null ? "" : message.getContent())
                    .append("\n");
        }
        return builder.toString().trim();
    }

    @Override
    public String buildMergedSystemPrompt(AiSession session, List<AiMessage> recentMessages) {
        StringBuilder builder = new StringBuilder(SYSTEM_PROMPT);
        if (session.getSummary() != null && !session.getSummary().isBlank()) {
            builder.append("\n\n【会话摘要】\n").append(session.getSummary());
        }
        if (session.getContextJson() != null && !session.getContextJson().isBlank()) {
            builder.append("\n\n【上下文快照】\n").append(session.getContextJson());
        }
        if (recentMessages != null && !recentMessages.isEmpty()) {
            builder.append("\n\n【最近对话】\n");
            for (AiMessage message : recentMessages) {
                builder.append(message.getRole()).append(": ")
                        .append(message.getContent() == null ? "" : message.getContent())
                        .append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public void updateContextAfterReply(AiSession session, String userMessage, String assistantMessage,
                                        List<Long> recommendedShopIds) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("lastUserMessage", userMessage == null ? "" : userMessage);
        context.put("lastAssistantMessage", assistantMessage == null ? "" : assistantMessage);
        context.put("recommendedShopIds", recommendedShopIds == null ? List.of() : recommendedShopIds);
        context.put("messageCount", session.getMessageCount());
        session.setContextJson(toJson(context));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("上下文序列化失败", e);
        }
    }
}
