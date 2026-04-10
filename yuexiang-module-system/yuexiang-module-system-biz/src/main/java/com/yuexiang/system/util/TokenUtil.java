package com.yuexiang.system.util;

import com.yuexiang.common.exception.UnauthorizedException;
import org.springframework.util.StringUtils;

public final class TokenUtil {

    private TokenUtil() {}

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从 Authorization 头中提取 token
     *
     * @throws UnauthorizedException 格式不合法时抛出
     */
    public static String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("缺少有效的 Authorization 头");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("Token 不能为空");
        }
        return token;
    }
}