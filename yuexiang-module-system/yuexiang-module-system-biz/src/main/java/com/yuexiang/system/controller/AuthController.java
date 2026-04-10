package com.yuexiang.system.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.system.domain.dto.*;
import com.yuexiang.system.domain.vo.*;
import com.yuexiang.system.service.AuthService;
import com.yuexiang.system.service.CaptchaService;
import com.yuexiang.system.util.IpUtil;
import com.yuexiang.system.util.TokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证接口", description = "登录、注册、验证码、Token 管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CaptchaService captchaService;
    private final AuthService authService;

    // ======================== 验证码 ========================

    @Operation(summary = "获取图形验证码", description = "返回 Base64 编码的验证码图片，有效期内使用")
    @GetMapping("/captcha")
    public CommonResult<CaptchaVO> getCaptcha() {
        return CommonResult.success(captchaService.generate());
    }

    @Operation(summary = "发送短信验证码", description = "需先通过图形验证码校验")
    @ApiResponse(responseCode = "200", description = "发送成功")
    @ApiResponse(responseCode = "400", description = "参数校验失败 / 图形验证码错误")
    @ApiResponse(responseCode = "429", description = "发送频率超限")
    @PostMapping("/sms-code")
    public CommonResult<Void> sendSmsCode(@Valid @RequestBody SmsCodeDTO dto,
                                          HttpServletRequest request) {
        authService.sendSmsCode(dto, IpUtil.getClientIp(request));
        return CommonResult.success();
    }

    // ======================== 登录 ========================

    @Operation(summary = "验证码登录", description = "未注册用户将自动注册")
    @PostMapping("/login/sms")
    public CommonResult<LoginVO> loginBySms(@Valid @RequestBody SmsLoginDTO dto) {
        return CommonResult.success(authService.loginBySms(dto));
    }

    @Operation(summary = "密码登录")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @ApiResponse(responseCode = "400", description = "手机号或密码错误")
    @ApiResponse(responseCode = "423", description = "账户已锁定")
    @PostMapping("/login/password")
    public CommonResult<LoginVO> loginByPassword(@Valid @RequestBody PasswordLoginDTO dto) {
        return CommonResult.success(authService.loginByPassword(dto));
    }

    // ======================== Token 管理 ========================

    @Operation(summary = "刷新 Token", description = "使用 refreshToken 换取新的令牌对")
    @PostMapping("/token/refresh")
    public CommonResult<TokenRefreshVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        return CommonResult.success(authService.refreshToken(dto));
    }

    @Operation(summary = "登出当前会话", description = "将当前 accessToken 加入黑名单")
    @PostMapping("/logout")
    public CommonResult<Void> logout(
            @Parameter(description = "Bearer Token", required = true)
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) LogoutDTO dto) {

        String accessToken = TokenUtil.extractToken(authorization);
        authService.logout(accessToken, dto);
        return CommonResult.success();
    }

    @Operation(summary = "吊销所有会话", description = "强制下线当前用户的所有客户端")
    @DeleteMapping("/sessions")
    public CommonResult<SessionRevokeVO> revokeAllSessions(
            @Parameter(description = "Bearer Token", required = true)
            @RequestHeader("Authorization") String authorization) {

        String accessToken = TokenUtil.extractToken(authorization);
        int count = authService.revokeAllSessions(accessToken);

        return CommonResult.success(
                SessionRevokeVO.builder()
                        .revokedCount(count)
                        .build()
        );
    }
}