package com.yuexiang.ai.service;

public interface AiSseService {

    String sessionEvent(String sessionId, String title);

    String thinkingEvent(String status, String message);

    String textChunkEvent(String content);

    String shopCardEvent(Object card);

    String voucherCardEvent(Object card);

    String blogCardEvent(Object card);

    String doneEvent(String sessionId, String title);

    String errorEvent(String message);
}
