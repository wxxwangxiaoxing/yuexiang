package com.yuexiang.ai.controller;

import com.yuexiang.ai.domain.dto.AiChatDTO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import com.yuexiang.ai.service.AiShopService;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.ai.service.AiChatService;
import com.yuexiang.framework.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(name = "AI探店")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiShopController {

    private final AiShopService aiShopService;
    private final AiChatService aiChatService;

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
}

