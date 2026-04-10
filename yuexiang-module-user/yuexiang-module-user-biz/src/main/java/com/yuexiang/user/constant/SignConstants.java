package com.yuexiang.user.constant;

import java.time.ZoneId;

public final class SignConstants {

    private SignConstants() {
    }

    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    public static final int REPAIR_COST_POINTS = 50;
    public static final int REPAIR_MAX_PER_MONTH = 3;
    public static final int REPAIR_WINDOW_DAYS = 7;

    public static final int NOT_DELETED = 0;
    public static final int CODE_TOO_FREQUENT = 50001;

    public static final long LOCK_EXPIRE_SECONDS = 5L;
    public static final long CONTINUOUS_CACHE_HOURS = 48L;
    public static final long MIN_BITMAP_CACHE_SECONDS = 86400L;
}
