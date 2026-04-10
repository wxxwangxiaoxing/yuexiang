package com.yuexiang.user.constant;

import java.time.format.DateTimeFormatter;

public final class UserProfileConstants {

    private UserProfileConstants() {
    }

    public static final int BONUS_POINTS = 30;
    public static final int BONUS_TRIGGER_DAYS = 7;
    public static final int MAX_MONTH_LOOKBACK = 13;

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
}
