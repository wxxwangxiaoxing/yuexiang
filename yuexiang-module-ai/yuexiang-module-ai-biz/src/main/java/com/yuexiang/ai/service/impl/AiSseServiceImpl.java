package com.yuexiang.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.ai.service.AiSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiSseServiceImpl implements AiSseService {

    private final ObjectMapper objectMapper;

    @Override
    public String sessionEvent(String sessionId, String title) {
        return toSse("session", Map.of("sessionId", sessionId, "title", title));
    }

    @Override
    public String thinkingEvent(String status, String message) {
        return toSse("thinking", Map.of("status", status, "message", message));
    }

    @Override
    public String textChunkEvent(String content) {
        return toSse("delta", Map.of("content", content));
    }

    @Override
    public String shopCardEvent(Object card) {
        return toSse("shop_card", card);
    }

    @Override
    public String voucherCardEvent(Object card) {
        return toSse("voucher_card", card);
    }

    @Override
    public String blogCardEvent(Object card) {
        return toSse("blog_card", card);
    }

    @Override
    public String doneEvent(String sessionId, String title) {
        return toSse("done", Map.of("sessionId", sessionId, "title", title));
    }

    @Override
    public String errorEvent(String message) {
        return toSse("error", Map.of("msg", message));
    }

    private String toSse(String event, Object data) {
        try {
            return "event: " + event + "\n" + "data: " + objectMapper.writeValueAsString(data) + "\n\n";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("SSE 序列化失败", e);
        }
    }
}
