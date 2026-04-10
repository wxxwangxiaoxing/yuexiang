package com.yuexiang.ai.service.impl;

import com.yuexiang.ai.domain.dto.AiChatDTO;
import com.yuexiang.ai.domain.dto.AiSendMessageDTO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import com.yuexiang.ai.service.AiConversationService;
import com.yuexiang.ai.service.AiSessionService;
import com.yuexiang.ai.service.AiShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AiShopServiceImpl implements AiShopService {

    private final AiConversationService aiConversationService;
    private final AiSessionService aiSessionService;

    @Override
    public AiSessionDetailVO chat(Long userId, AiChatDTO dto) {
        return aiConversationService.sendMessage(userId, toSendMessageDTO(dto));
    }

    @Override
    public Flux<String> chatStream(Long userId, AiChatDTO dto) {
        return aiConversationService.sendMessageStream(userId, toSendMessageDTO(dto));
    }

    @Override
    public AiSessionDetailVO getSessionDetail(String sessionId, Long userId) {
        return aiSessionService.getSessionDetail(userId, sessionId);
    }

    @Override
    public boolean deleteSession(String sessionId, Long userId) {
        return aiSessionService.deleteSession(userId, sessionId);
    }

    private AiSendMessageDTO toSendMessageDTO(AiChatDTO dto) {
        AiSendMessageDTO sendMessageDTO = new AiSendMessageDTO();
        sendMessageDTO.setSessionId(dto.getSessionId());
        sendMessageDTO.setQuestion(dto.getQuestion());
        sendMessageDTO.setLongitude(dto.getLng());
        sendMessageDTO.setLatitude(dto.getLat());
        return sendMessageDTO;
    }
}
