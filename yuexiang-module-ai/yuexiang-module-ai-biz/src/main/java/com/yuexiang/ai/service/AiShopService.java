package com.yuexiang.ai.service;

import com.yuexiang.ai.domain.dto.AiChatDTO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import reactor.core.publisher.Flux;

public interface AiShopService {

    AiSessionDetailVO chat(Long userId, AiChatDTO dto);

    Flux<String> chatStream(Long userId, AiChatDTO dto);

    AiSessionDetailVO getSessionDetail(String sessionId, Long userId);

    boolean deleteSession(String sessionId, Long userId);
}
