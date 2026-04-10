package com.yuexiang.user.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.user.domain.dto.*;
import com.yuexiang.user.domain.vo.*;
import com.yuexiang.user.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户账户管理", description = "用户信息、密码、实名认证、账户注销等接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    // ==================== 用户信息 ====================

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的完整信息")
    @GetMapping("/me")
    public CommonResult<UserMeVO> getMe() {
        return CommonResult.success(userAccountService.getMe(getCurrentUserId()));
    }

    @Operation(summary = "获取用户公开信息", description = "根据用户ID获取公开可见的信息")
    @GetMapping("/{userId}")
    public CommonResult<UserPublicVO> getPublicInfo(
            @PathVariable @Parameter(description = "用户ID", required = true) Long userId) {
        return CommonResult.success(userAccountService.getPublicInfo(userId));
    }

    @Operation(summary = "修改基本信息", description = "修改昵称、头像等基本资料")
    @PutMapping("/info")
    public CommonResult<Void> updateInfo(@Valid @RequestBody UpdateUserInfoDTO dto) {
        userAccountService.updateInfo(getCurrentUserId(), dto);
        return CommonResult.success();
    }

    // ==================== 手机号 ====================

    @Operation(summary = "修改手机号", description = "需验证新手机号短信验证码")
    @PutMapping("/phone")
    public CommonResult<Void> updatePhone(@Valid @RequestBody UpdatePhoneDTO dto) {
        userAccountService.updatePhone(getCurrentUserId(), dto);
        return CommonResult.success();
    }

    // ==================== 登录密码 ====================

    @Operation(summary = "设置密码", description = "首次设置登录密码（适用于手机号验证码注册的用户）")
    @PostMapping("/password")
    public CommonResult<Void> setPassword(@Valid @RequestBody SetPasswordDTO dto) {
        userAccountService.setPassword(getCurrentUserId(), dto);
        return CommonResult.success();
    }

    @Operation(summary = "修改密码", description = "已有密码的用户修改密码")
    @PutMapping("/password")
    public CommonResult<PasswordResultVO> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        return CommonResult.success(userAccountService.updatePassword(getCurrentUserId(), dto));
    }

    @Operation(summary = "重置密码", description = "通过短信验证码重置密码，无需登录")
    @PostMapping("/password/reset")
    @PreAuthorize("permitAll()")
    public CommonResult<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userAccountService.resetPassword(dto);
        return CommonResult.success();
    }

    // ==================== 支付密码 ====================

    @Operation(summary = "设置支付密码")
    @PostMapping("/pay-password")
    public CommonResult<Void> setPayPassword(@Valid @RequestBody SetPayPasswordDTO dto) {
        userAccountService.setPayPassword(getCurrentUserId(), dto);
        return CommonResult.success();
    }

    @Operation(summary = "修改支付密码")
    @PutMapping("/pay-password")
    public CommonResult<Void> updatePayPassword(@Valid @RequestBody UpdatePayPasswordDTO dto) {
        userAccountService.updatePayPassword(getCurrentUserId(), dto);
        return CommonResult.success();
    }

    @Operation(summary = "重置支付密码", description = "通过短信验证码重置支付密码")
    @PostMapping("/pay-password/reset")
    public CommonResult<Void> resetPayPassword(@Valid @RequestBody ResetPayPasswordDTO dto) {
        userAccountService.resetPayPassword(getCurrentUserId(), dto);
        return CommonResult.success();
    }

    // ==================== 实名认证 ====================

    @Operation(summary = "提交实名认证")
    @ApiResponse(responseCode = "200", description = "提交成功，返回认证流水信息")
    @PostMapping("/real-name")
    public CommonResult<RealNameSubmitVO> submitRealName(@Valid @RequestBody RealNameDTO dto) {
        return CommonResult.success(userAccountService.submitRealName(getCurrentUserId(), dto));
    }

    @Operation(summary = "查询实名认证状态")
    @GetMapping("/real-name")
    public CommonResult<RealNameVO> getRealName() {
        return CommonResult.success(userAccountService.getRealName(getCurrentUserId()));
    }

    // ==================== 账户注销 ====================

    @Operation(summary = "申请注销账户")
    @PostMapping("/cancel")
    public CommonResult<CancelResultVO> applyCancel(@Valid @RequestBody CancelAccountDTO dto) {
        return CommonResult.success(userAccountService.applyCancel(getCurrentUserId(), dto));
    }

    @Operation(summary = "撤销注销申请")
    @DeleteMapping("/cancel")
    public CommonResult<Void> revokeCancel(@Valid @RequestBody RevokeCancelDTO dto) {
        userAccountService.revokeCancel(getCurrentUserId(), dto);
        return CommonResult.success();
    }

    // ==================== 私有方法 ====================

    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }
}
