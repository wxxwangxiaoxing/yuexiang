package com.yuexiang.user.support;

import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.constant.SignConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SignComputeSupport {

    private final SignCacheSupport signCacheSupport;

    public List<Integer> parseSignedDays(int signBitmap, int totalDaysInMonth) {
        List<Integer> days = new ArrayList<>();
        for (int day = 1; day <= totalDaysInMonth; day++) {
            if (isSigned(signBitmap, day)) {
                days.add(day);
            }
        }
        return days;
    }

    public int calcContinuousDaysEndingAt(Long userId, LocalDate endDate) {
        int continuous = 0;
        LocalDate checkDate = endDate;

        int cachedYear = -1;
        int cachedMonth = -1;
        Integer cachedBitmap = null;

        while (true) {
            int year = checkDate.getYear();
            int month = checkDate.getMonthValue();
            int day = checkDate.getDayOfMonth();

            if (year != cachedYear || month != cachedMonth) {
                cachedBitmap = signCacheSupport.getSignBitmap(userId, year, month);
                cachedYear = year;
                cachedMonth = month;
            }

            if (cachedBitmap == null) {
                break;
            }

            if (isSigned(cachedBitmap, day)) {
                continuous++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }

            if (continuous >= 365) {
                break;
            }
        }

        return continuous;
    }

    public void validateRepairDate(LocalDate repairDate, LocalDate today) {
        if (!repairDate.isBefore(today)) {
            throw new BusinessException(40024, "不能补签今天或未来日期");
        }

        long daysBetween = ChronoUnit.DAYS.between(repairDate, today);
        if (daysBetween > SignConstants.REPAIR_WINDOW_DAYS) {
            throw new BusinessException(
                    40023,
                    String.format("仅支持补签最近%d天内的日期", SignConstants.REPAIR_WINDOW_DAYS)
            );
        }
    }

    public int calcCurrentContinuousDays(Long userId, LocalDate today, int year, int month, int bitmap) {
        int continuousDays = calcContinuousDaysEndingAt(userId, today.minusDays(1));
        if (isCurrentMonth(year, month, today) && isSigned(bitmap, today.getDayOfMonth())) {
            continuousDays++;
        }
        return continuousDays;
    }

    public boolean isCurrentMonth(int year, int month, LocalDate today) {
        return year == today.getYear() && month == today.getMonthValue();
    }

    public boolean isSigned(int bitmap, int day) {
        return ((bitmap >>> (day - 1)) & 1) == 1;
    }

    public int markSigned(int bitmap, int day) {
        return bitmap | (1 << (day - 1));
    }
}
