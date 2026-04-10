package com.yuexiang.user.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.user.api.UserWalletService;
import com.yuexiang.user.domain.vo.SignCalendarVO;
import com.yuexiang.user.domain.vo.SignResultVO;
import com.yuexiang.user.domain.vo.UserProfileVO;
import com.yuexiang.user.domain.vo.UserWalletVO;
import com.yuexiang.user.api.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserWalletService userWalletService;

    @Operation(summary = "个人中心首页")
    @GetMapping("/profile")
    public CommonResult<UserProfileVO> getProfile() {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userProfileService.getProfile(userId));
    }

    @Operation(summary = "我的钱包")
    @GetMapping("/wallet")
    public CommonResult<UserWalletVO> getWallet() {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userWalletService.getWalletInfo(userId));
    }

    @Operation(summary = "签到日历")
    @GetMapping("/sign/calendar")
    public CommonResult<SignCalendarVO> getSignCalendar(
            @Parameter(description = "年份") @RequestParam(value = "year", required = false) Integer year,
            @Parameter(description = "月份") @RequestParam(value = "month", required = false) Integer month) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userProfileService.getSignCalendar(userId, year, month));
    }

    @Operation(summary = "执行签到")
    @PostMapping("/sign")
    public CommonResult<SignResultVO> sign() {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userProfileService.sign(userId));
    }
}
