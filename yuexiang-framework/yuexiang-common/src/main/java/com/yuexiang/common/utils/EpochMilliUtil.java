package com.yuexiang.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 毫秒级时间戳与 Java Time 互转工具
 */
public final class EpochMilliUtil {

    private EpochMilliUtil() {}

    private static final ZoneId ZONE = ZoneId.systemDefault();

    public static LocalDateTime toLocalDateTime(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZONE).toLocalDateTime();
    }

    public static LocalDate toLocalDate(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZONE).toLocalDate();
    }

    /**
     * 计算距今剩余天数（过期则返回负数，最小 -1）
     */
    public static int remainDaysFromNow(long epochMilli) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), toLocalDate(epochMilli));
        return (int) Math.max(-1, days);
    }

    /**
     * 日期转为友好描述：今天 / 昨天 / MM-dd
     */
    public static String toFriendlyDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) {
            return "今天";
        }
        if (date.equals(today.minusDays(1))) {
            return "昨天";
        }
        return String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
    }
}