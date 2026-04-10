package com.yuexiang.system.constant;

public final class AuthRedisConstants {

    private AuthRedisConstants() {}

    public static final String CAPTCHA_KEY = "captcha:%s";
    public static final int CAPTCHA_EXPIRE_SECONDS = 300;

    public static final String SMS_RATE_MIN_KEY = "rate:sms:min:%s";
    public static final String SMS_RATE_DAY_KEY = "rate:sms:day:%s";
    public static final String SMS_RATE_IP_KEY = "rate:sms:ip:%s";
    public static final int SMS_MIN_LIMIT = 1;
    public static final int SMS_DAY_LIMIT = 10;
    public static final int SMS_IP_DAY_LIMIT = 100;

    public static final String SMS_VERIFY_ERR_KEY = "sms:verify:err:%s";
    public static final int SMS_VERIFY_MAX_ERR = 5;
    public static final int SMS_VERIFY_ERR_EXPIRE = 300;

    public static final String LOGIN_PWD_ERR_KEY = "login:pwd:err:%s";
    public static final String LOGIN_PWD_LOCK_KEY = "login:pwd:lock:%s";
    public static final int LOGIN_PWD_MAX_ERR = 5;
    public static final int LOGIN_PWD_LOCK_MINUTES = 30;

}
