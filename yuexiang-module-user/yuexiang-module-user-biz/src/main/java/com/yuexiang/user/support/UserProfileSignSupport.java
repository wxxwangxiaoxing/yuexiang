package com.yuexiang.user.support;

import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.user.constant.UserProfileConstants;
import com.yuexiang.user.domain.entity.Sign;
import com.yuexiang.user.mapper.SignMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileSignSupport {

    private final SignMapper signMapper;

    public Sign getUserMonthSign(Long userId, int year, int month) {
        return signMapper.selectByUserMonth(userId, year, month);
    }

    public boolean isBitSet(long bitmap, int day) {
        return ((bitmap >>> (day - 1)) & 1) == 1;
    }

    public boolean isBitSetSafe(Sign sign, int day) {
        return sign != null && isBitSet(sign.getSignBitmap(), day);
    }

    public List<Integer> parseBitmap(long bitmap) {
        List<Integer> days = new ArrayList<>(Long.bitCount(bitmap));
        for (long b = bitmap; b != 0; b &= b - 1) {
            days.add(Long.numberOfTrailingZeros(b) + 1);
        }
        return days;
    }

    public int calcContinuousDays(Long userId, LocalDate today, boolean includesToday, Sign preloaded) {
        LocalDate cursor = includesToday ? today : today.minusDays(1);
        int total = 0;

        for (int i = 0; i < UserProfileConstants.MAX_MONTH_LOOKBACK; i++) {
            int y = cursor.getYear();
            int m = cursor.getMonthValue();
            int endDay = cursor.getDayOfMonth();
            Sign sign = (preloaded != null
                    && preloaded.getYear() == y
                    && preloaded.getMonth() == m)
                    ? preloaded
                    : signMapper.selectByUserMonth(userId, y, m);

            if (sign == null) {
                break;
            }

            int consecutive = countConsecutiveEndingAt(sign.getSignBitmap(), endDay);
            total += consecutive;
            if (consecutive < endDay) {
                break;
            }

            cursor = LocalDate.of(y, m, 1).minusDays(1);
        }

        return total;
    }

    public int calculatePoints(int continuousDays) {
        if (continuousDays >= UserProfileConstants.BONUS_TRIGGER_DAYS) {
            return 50;
        }
        if (continuousDays >= 5) {
            return 20;
        }
        if (continuousDays >= 3) {
            return 15;
        }
        return 10;
    }

    public int calculateMonthTotalPoints(Sign sign) {
        if (sign == null || sign.getSignDays() == 0) {
            return 0;
        }
        return sign.getSignDays() * 10;
    }

    public Sign getOrCreateMonthSign(Long userId, int year, int month) {
        Sign sign = signMapper.selectByUserMonth(userId, year, month);
        if (sign != null) {
            return sign;
        }

        try {
            sign = new Sign();
            sign.setUserId(userId);
            sign.setYear(year);
            sign.setMonth(month);
            sign.setSignBitmap(0);
            sign.setSignDays(0);
            signMapper.insert(sign);
            log.debug("初始化签到记录: userId={}, {}-{}", userId, year, month);
            return sign;
        } catch (Exception e) {
            log.debug("签到记录并发创建冲突: userId={}", userId);
            sign = signMapper.selectByUserMonth(userId, year, month);
            if (sign == null) {
                throw new BadRequestException("初始化签到记录失败");
            }
            return sign;
        }
    }

    private int countConsecutiveEndingAt(long bitmap, int endDay) {
        if (endDay <= 0) {
            return 0;
        }
        long mask = (1L << endDay) - 1;
        long masked = bitmap & mask;
        long gaps = (~masked) & mask;
        if (gaps == 0) {
            return endDay;
        }
        int highestGap = 63 - Long.numberOfLeadingZeros(gaps);
        return (endDay - 1) - highestGap;
    }
}
