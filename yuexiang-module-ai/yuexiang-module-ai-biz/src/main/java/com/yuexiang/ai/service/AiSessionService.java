package com.yuexiang.ai.service;

import com.yuexiang.ai.domain.dto.AiCreateSessionDTO;
import com.yuexiang.ai.domain.dto.AiSessionQueryDTO;
import com.yuexiang.ai.domain.dto.AiSendMessageDTO;
import com.yuexiang.ai.domain.entity.AiSession;
import com.yuexiang.ai.domain.vo.AiMessageVO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import com.yuexiang.ai.domain.vo.AiSessionListItemVO;
import com.yuexiang.common.pojo.PageResult;

import java.util.List;

public interface AiSessionService {

    AiSessionDetailVO createSession(Long userId, AiCreateSessionDTO dto);

    AiSession getOrCreateSession(Long userId, AiSendMessageDTO dto);

    AiSession getRequiredSession(Long userId, String sessionId);

    AiSessionDetailVO getSessionDetail(Long userId, String sessionId);

    List<AiMessageVO> getSessionHistory(Long userId, String sessionId);

    PageResult<AiSessionListItemVO> listSessions(Long userId, AiSessionQueryDTO queryDTO);

    boolean deleteSession(Long userId, String sessionId);

    void increaseMessageCount(AiSession session, int increment, List<Long> recommendedShopIds);
}
