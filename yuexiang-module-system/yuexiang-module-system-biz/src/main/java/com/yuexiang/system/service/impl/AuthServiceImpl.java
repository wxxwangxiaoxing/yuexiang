package com.yuexiang.system.service.impl;

import com.yuexiang.common.exception.*;
import com.yuexiang.framework.security.token.JwtTokenProvider;
import com.yuexiang.system.constant.AuthConstants;
import com.yuexiang.system.constant.SmsCodeConstants;
import com.yuexiang.system.constant.UserStatusConstants;
import com.yuexiang.system.domain.dto.*;
import com.yuexiang.system.domain.entity.SmsCode;
import com.yuexiang.system.domain.vo.LoginVO;
import com.yuexiang.system.domain.vo.TokenRefreshVO;
import com.yuexiang.system.domain.vo.UserSimpleVO;
import com.yuexiang.system.mapper.SmsCodeMapper;
import com.yuexiang.system.service.AuthService;
import com.yuexiang.system.service.CaptchaService;
import com.yuexiang.system.support.AuthRedisSupport;
import com.yuexiang.system.util.PasswordUtil;
import com.yuexiang.system.util.ValidationUtil;
import com.yuexiang.user.api.UserAuthService;
import com.yuexiang.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    // ======================== 依赖注入 ========================

    private final SmsCodeMapper smsCodeMapper;
    private final UserAuthService userAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CaptchaService captchaService;
    private final AuthRedisSupport authRedisSupport;

    // ======================== 公开接口 ========================

    @Override
    public void sendSmsCode(SmsCodeDTO dto, String ip) {
        String phone = dto.getPhone();
        int type = dto.getType();

        validatePhone(phone);

        if (!captchaService.verify(dto.getCaptchaId(), dto.getCaptchaCode())) {
            throw new BadRequestException("图形验证码错误或过期");
        }

        authRedisSupport.checkSmsRateLimit(phone, ip);
        checkPhoneForSmsType(phone, type);
        saveSmsCode(phone, type, ip);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginBySms(SmsLoginDTO dto) {
        String phone = dto.getPhone();
        validatePhone(phone);
        //1.如果验证码 
        verifySmsCode(phone, dto.getCode(), SmsCodeConstants.TYPE_LOGIN);

        User user = userAuthService.findByPhone(phone);
        if (user == null) {
            user = registerUser(phone);
        } else {
            checkUserStatus(user);
        }

        return buildLoginVO(user);
    }

    @Override
    public LoginVO loginByPassword(PasswordLoginDTO dto) {
        String phone = dto.getPhone();
        validatePhone(phone);

        authRedisSupport.checkPasswordLock(phone);

        User user = userAuthService.findByPhone(phone);
        if (user == null) {
            throw new BadRequestException("手机号或密码错误");
        }

        if (!StringUtils.hasText(user.getPassword())) {
            throw new BadRequestException("该账户未设置密码，请使用验证码登录");
        }

        checkUserStatus(user);
        verifyPassword(phone, dto.getPassword(), user.getPassword());

        return buildLoginVO(user);
    }

    @Override
    public TokenRefreshVO refreshToken(RefreshTokenDTO dto) {
        var tokenPair = jwtTokenProvider.refreshToken(dto.getRefreshToken());

        return TokenRefreshVO.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getExpiresIn())
                .build();
    }

    @Override
    public void logout(String accessToken, LogoutDTO dto) {
        jwtTokenProvider.addToBlacklist(accessToken);
        if (dto != null && StringUtils.hasText(dto.getRefreshToken())) {
            try {
                jwtTokenProvider.addToBlacklist(dto.getRefreshToken());
            } catch (Exception e) {
                log.warn("登出时处理 refreshToken 失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public int revokeAllSessions(String accessToken) {
        Long userId = jwtTokenProvider.getUserId(accessToken);
        return jwtTokenProvider.revokeAllSessions(userId);
    }

    // ======================== 登录结果构建 ========================

    private LoginVO buildLoginVO(User user) {
        var tokenPair = jwtTokenProvider.generateTokenPair(user.getId());

        UserSimpleVO userVO = UserSimpleVO.builder()
                .id(user.getId())
                .nickName(user.getNickName())
                .avatar(user.getAvatar())
                .build();

        return LoginVO.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getExpiresIn())
                .absoluteExpiresIn(tokenPair.getAbsoluteExpiresIn())
                .user(userVO)
                .build();
    }

    // ======================== 手机号与验证 ========================

    private void validatePhone(String phone) {
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new BadRequestException("手机号格式不正确");
        }
    }

    private void checkPhoneForSmsType(String phone, int type) {
        User user = userAuthService.findByPhone(phone);

        if (type == SmsCodeConstants.TYPE_REGISTER && user != null) {
            throw new BadRequestException("该手机号已注册");
        }

        // 登录/重置密码场景：不强制要求已注册，loginBySms 支持自动注册
    }

    private void checkUserStatus(User user) {
        if (user.getStatus() == UserStatusConstants.FROZEN) {
            throw new ForbiddenException("账户已被冻结");
        }
        if (user.getStatus() == UserStatusConstants.BANNED) {
            throw new ForbiddenException("账户已被封禁");
        }
    }

    // ======================== 短信验证码 ========================

    private void saveSmsCode(String phone, int type, String ip) {
        String code = PasswordUtil.generateSmsCode();
        LocalDateTime now = LocalDateTime.now();

        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setType(type);
        smsCode.setStatus(SmsCodeConstants.STATUS_UNUSED);
        smsCode.setIp(ip);
        smsCode.setExpireTime(now.plusMinutes(AuthConstants.SMS_CODE_EXPIRE_MINUTES));
        smsCode.setCreateTime(now);
        smsCodeMapper.insert(smsCode);

        // TODO: 对接真实短信服务商（当前仅日志输出）
        log.info("短信验证码已发送: phone={}, type={}, code={}", phone, type, code);
    }

    private void verifySmsCode(String phone, String code, int type) {
        SmsCode smsCode = smsCodeMapper.selectLatest(phone, type);

        if (smsCode == null) {
            throw new BadRequestException("验证码不存在，请重新获取");
        }

        if (smsCode.getStatus() == SmsCodeConstants.STATUS_USED) {
            throw new BadRequestException("验证码已使用，请重新获取");
        }

        if (smsCode.getExpireTime().isBefore(LocalDateTime.now())) {
            smsCodeMapper.updateStatus(smsCode.getId(), SmsCodeConstants.STATUS_EXPIRED);
            throw new BadRequestException("验证码已过期，请重新获取");
        }

        authRedisSupport.checkSmsVerifyAttempts(smsCode.getId());

        if (!smsCode.getCode().equals(code)) {
            authRedisSupport.recordSmsVerifyError(smsCode.getId());
        }

        smsCodeMapper.updateStatus(smsCode.getId(), SmsCodeConstants.STATUS_USED);
    }

    // ======================== 密码登录 ========================

    private void verifyPassword(String phone, String rawPassword, String encodedPassword) {
        if (PasswordUtil.matches(rawPassword, encodedPassword)) {
            authRedisSupport.clearPasswordError(phone);
            return;
        }

        authRedisSupport.handlePasswordError(phone);
    }

    // ======================== 用户注册 ========================

    @Transactional(rollbackFor = Exception.class)
    protected User registerUser(String phone) {
        return userAuthService.registerUser(phone, PasswordUtil.generateNickname(), AuthConstants.DEFAULT_AVATAR);
    }

}
