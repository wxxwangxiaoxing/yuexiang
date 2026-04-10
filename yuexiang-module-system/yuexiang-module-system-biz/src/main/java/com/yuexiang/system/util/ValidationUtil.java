package com.yuexiang.system.util;

import com.yuexiang.system.constant.AuthConstants;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private ValidationUtil() {}

    private static final Pattern GLOBAL_PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,14}$");
    private static final Pattern CN_PHONE_PATTERN = Pattern.compile("^\\+861[3-9]\\d{9}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d!@#$%^&*()_+\\-=]{" + 
            AuthConstants.PASSWORD_MIN_LENGTH + "," + AuthConstants.PASSWORD_MAX_LENGTH + "}$"
    );

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return GLOBAL_PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isCnPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return CN_PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isWeakPassword(String password) {
        if (password == null || password.isEmpty()) {
            return true;
        }
        String lower = password.toLowerCase();
        for (String weak : AuthConstants.WEAK_PASSWORDS) {
            if (lower.equals(weak.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
