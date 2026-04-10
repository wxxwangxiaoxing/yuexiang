package com.yuexiang.system.service;

import com.yuexiang.system.domain.dto.*;
import com.yuexiang.system.domain.vo.LoginVO;
import com.yuexiang.system.domain.vo.TokenRefreshVO;

public interface AuthService {

    void sendSmsCode(SmsCodeDTO dto, String ip);

    LoginVO loginBySms(SmsLoginDTO dto);

    LoginVO loginByPassword(PasswordLoginDTO dto);

    TokenRefreshVO refreshToken(RefreshTokenDTO dto);

    void logout(String accessToken, LogoutDTO dto);

    int revokeAllSessions(String accessToken);
}
