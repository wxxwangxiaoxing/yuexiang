package com.yuexiang.system.constant;

public final class SmsRedisConstants {

    private SmsRedisConstants() {}

    public static final String CODE_KEY = "login:code:%s";
    public static final String CODE_FAIL_KEY = "login:code:fail:%s";
    public static final String COOLDOWN_KEY = "login:sms:cooldown:%s";
    public static final String DAILY_PHONE_KEY = "login:sms:daily:phone:%s:%s";
    public static final String DAILY_IP_KEY = "login:sms:daily:ip:%s:%s";

    public static final int COOLDOWN_SECONDS = 60;
    public static final int CODE_TTL_SECONDS = 120;
    public static final int DAILY_PHONE_LIMIT = 10;
    public static final int DAILY_IP_LIMIT = 100;
    public static final int MAX_FAIL_COUNT = 5;
}
