package com.yuexiang.framework.security.constant;

public final class JwtRedisConstants {

    private JwtRedisConstants() {}

    public static final String AUTH_REFRESH_KEY = "auth:refresh:%s";
    public static final String AUTH_BLACKLIST_KEY = "auth:blacklist:%s";
    public static final String AUTH_USER_SESSIONS_KEY = "auth:user_sessions:%s";
}
