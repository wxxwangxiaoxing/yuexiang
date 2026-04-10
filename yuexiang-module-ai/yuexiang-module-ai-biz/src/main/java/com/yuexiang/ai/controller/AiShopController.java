package com.yuexiang.ai.controller;

import com.yuexiang.ai.domain.dto.AiChatDTO;
import com.yuexiang.ai.domain.dto.AiCreateSessionDTO;
import com.yuexiang.ai.domain.dto.AiSessionQueryDTO;
import com.yuexiang.ai.domain.dto.AiSendMessageDTO;
import com.yuexiang.ai.domain.vo.AiHotQuestionVO;
import com.yuexiang.ai.domain.vo.AiMessageVO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import com.yuexiang.ai.domain.vo.AiSessionListItemVO;
import com.yuexiang.ai.service.AiConversationService;
import com.yuexiang.ai.service.AiHotQuestionService;
import com.yuexiang.ai.service.AiShopService;
import com.yuexiang.ai.service.AiSessionService;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.framework.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "AI探店")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiShopController {

    private final AiShopService aiShopService;
    private final AiConversationService aiConversationService;
    private final AiSessionService aiSessionService;
    private final AiHotQuestionService aiHotQuestionService;

    @Operation(summary = "AI对话")
    @PostMapping("/chat")
    public CommonResult<AiSessionDetailVO> chat(@RequestBody AiChatDTO dto) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiShopService.chat(userId, dto));
    }

    @Operation(summary = "AI流式对话(SSE)")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody AiChatDTO dto) {
        Long userId = UserContext.getUserId();
        return aiShopService.chatStream(userId, dto);
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/session/{sessionId}")
    public CommonResult<AiSessionDetailVO> getSessionDetail(
            @Parameter(description = "会话ID") @PathVariable("sessionId") String sessionId) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiShopService.getSessionDetail(sessionId, userId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/session/{sessionId}")
    public CommonResult<Boolean> deleteSession(
            @Parameter(description = "会话ID") @PathVariable("sessionId") String sessionId) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiShopService.deleteSession(sessionId, userId));
    }

    @Operation(summary = "创建AI会话")
    @PostMapping("/chat/session")
    public CommonResult<AiSessionDetailVO> createSession(@RequestBody(required = false) AiCreateSessionDTO dto) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiSessionService.createSession(userId, dto));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/chat/send")
    public CommonResult<AiSessionDetailVO> sendMessage(@RequestBody AiSendMessageDTO dto) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiConversationService.sendMessage(userId, dto));
    }

    @Operation(summary = "发送消息(SSE)")
    @PostMapping(value = "/chat/send/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@RequestBody AiSendMessageDTO dto) {
        Long userId = UserContext.getUserId();
        return aiConversationService.sendMessageStream(userId, dto);
    }

    @Operation(summary = "获取会话历史")
    @GetMapping("/chat/history/{sessionId}")
    public CommonResult<List<AiMessageVO>> getSessionHistory(
            @Parameter(description = "会话ID") @PathVariable("sessionId") String sessionId) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiSessionService.getSessionHistory(userId, sessionId));
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/chat/sessions")
    public CommonResult<PageResult<AiSessionListItemVO>> listSessions(AiSessionQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiSessionService.listSessions(userId, queryDTO));
    }

    @Operation(summary = "获取热门问题")
    @GetMapping("/chat/hot-questions")
    public CommonResult<List<AiHotQuestionVO>> listHotQuestions() {
        return CommonResult.success(aiHotQuestionService.listHotQuestions());
    }

    @Operation(summary = "删除AI会话")
    @DeleteMapping("/chat/session/{sessionId}")
    public CommonResult<Boolean> deleteChatSession(
            @Parameter(description = "会话ID") @PathVariable("sessionId") String sessionId) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(aiSessionService.deleteSession(userId, sessionId));
    }
}
