package com.yuexiang.system.constant;

public final class AuthConstants {

    private AuthConstants() {}

    public static final String DEFAULT_AVATAR = "default/avatar.png";
    public static final String NICKNAME_PREFIX = "user_";
    public static final int NICKNAME_RANDOM_LENGTH = 6;

    public static final String CHARS_FOR_NICKNAME = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String CHARS_FOR_CAPTCHA = "abcdefghjkmnpqrstuvwxyz23456789";

    public static final int CAPTCHA_WIDTH = 120;
    public static final int CAPTCHA_HEIGHT = 40;
    public static final int CAPTCHA_LENGTH = 4;

    public static final int SMS_CODE_LENGTH = 6;
    public static final int SMS_CODE_EXPIRE_MINUTES = 2;

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final int BCRYPT_STRENGTH = 10;

    public static final String[] WEAK_PASSWORDS = {
            "password", "123456", "12345678", "qwerty", "abc123",
            "Password1", "Admin123", "Welcome1", "Passw0rd"
    };
}
