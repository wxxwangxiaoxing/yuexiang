package com.yuexiang.blog.controller;

import com.yuexiang.blog.domain.vo.FollowVO;
import com.yuexiang.blog.service.FollowService;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户关注", description = "用户关注相关接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "关注/取消关注", description = "切换关注状态")
    @PutMapping("/follow/{userId}")
    public CommonResult<FollowVO> toggleFollow(
            @Parameter(description = "目标用户ID") @PathVariable("userId") Long userId) {
        Long currentUserId = UserContext.getUserId();
        FollowVO vo = followService.toggleFollow(userId, currentUserId);
        return CommonResult.success(vo);
    }
}

