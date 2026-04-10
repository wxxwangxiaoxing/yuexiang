package com.yuexiang.ai.service;

import com.yuexiang.ai.domain.dto.AiSendMessageDTO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import reactor.core.publisher.Flux;

public interface AiConversationService {

    AiSessionDetailVO sendMessage(Long userId, AiSendMessageDTO dto);

    Flux<String> sendMessageStream(Long userId, AiSendMessageDTO dto);
}
