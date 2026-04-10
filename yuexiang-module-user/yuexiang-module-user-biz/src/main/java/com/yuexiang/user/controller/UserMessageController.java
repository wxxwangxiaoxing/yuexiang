package com.yuexiang.user.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.user.domain.vo.MessageVO;
import com.yuexiang.user.service.UserMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户消息")
@RestController
@RequestMapping("/api/user/message")
@RequiredArgsConstructor
public class UserMessageController {

    private final UserMessageService userMessageService;

    @Operation(summary = "获取消息列表")
    @GetMapping("/list")
    public CommonResult<List<MessageVO>> getMessages(
            @Parameter(description = "消息类型：1系统通知 2点赞收藏 3新增关注 4评论和@") @RequestParam(value = "type", required = false) Integer type) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userMessageService.getMessages(userId, type));
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public CommonResult<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userMessageService.getUnreadCount(userId));
    }

    @Operation(summary = "全部标记为已读")
    @PostMapping("/mark-all-read")
    public CommonResult<Integer> markAllRead() {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userMessageService.markAllRead(userId));
    }
}
