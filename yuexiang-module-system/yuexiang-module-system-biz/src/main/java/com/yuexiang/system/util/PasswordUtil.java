package com.yuexiang.system.util;

import com.yuexiang.system.constant.AuthConstants;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Random;

public final class PasswordUtil {

    private static final Random RANDOM = new Random();

    private PasswordUtil() {}

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(AuthConstants.BCRYPT_STRENGTH));
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    public static boolean needsUpgrade(String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        return !encodedPassword.startsWith("$2a$" + AuthConstants.BCRYPT_STRENGTH);
    }

    public static String generateNickname() {
        StringBuilder sb = new StringBuilder(AuthConstants.NICKNAME_PREFIX);
        for (int i = 0; i < AuthConstants.NICKNAME_RANDOM_LENGTH; i++) {
            sb.append(AuthConstants.CHARS_FOR_NICKNAME.charAt(RANDOM.nextInt(AuthConstants.CHARS_FOR_NICKNAME.length())));
        }
        return sb.toString();
    }

    public static String generateSmsCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < AuthConstants.SMS_CODE_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
