package com.yuexiang.user.constant;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class UserAccountConstants {

    private UserAccountConstants() {
    }

    public static final String PAY_PWD_ERR_KEY = "pay_pwd_err:%d";
    public static final String PAY_PWD_LOCK_KEY = "pay_pwd_lock:%d";

    public static final int MAX_ERR_L1 = 3;
    public static final int MAX_ERR_L2 = 5;
    public static final int MAX_ERR_L3 = 10;
    public static final int LOCK_MINUTES_L1 = 120;
    public static final int LOCK_MINUTES_L2 = 1440;

    public static final int COOLING_DAYS = 7;
    public static final int BCRYPT_STRENGTH = 10;

    public static final Pattern LOGIN_PWD_PATTERN =
            Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$");
    public static final Pattern PAY_PWD_PATTERN =
            Pattern.compile("^\\d{6}$");

    public static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
}
