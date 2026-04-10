package com.yuexiang.user.support;

import com.yuexiang.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignComputeSupportTest {

    @Mock
    private SignCacheSupport signCacheSupport;

    @InjectMocks
    private SignComputeSupport signComputeSupport;

    @Test
    void parseSignedDaysReturnsAllSignedDayNumbers() {
        List<Integer> signedDays = signComputeSupport.parseSignedDays(0b10101, 5);

        assertEquals(List.of(1, 3, 5), signedDays);
    }

    @Test
    void calcContinuousDaysEndingAtSupportsCrossMonthLookup() {
        Long userId = 1001L;
        when(signCacheSupport.getSignBitmap(userId, 2026, 4)).thenReturn(0b11);
        when(signCacheSupport.getSignBitmap(userId, 2026, 3)).thenReturn(1 << 30);

        int continuousDays = signComputeSupport.calcContinuousDaysEndingAt(userId, LocalDate.of(2026, 4, 2));

        assertEquals(3, continuousDays);
    }

    @Test
    void validateRepairDateRejectsTodayAndFutureDates() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> signComputeSupport.validateRepairDate(LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 4))
        );

        assertEquals(40024, exception.getCode());
    }

    @Test
    void calcCurrentContinuousDaysIncludesTodayWhenSigned() {
        Long userId = 1001L;
        LocalDate today = LocalDate.of(2026, 4, 4);
        when(signCacheSupport.getSignBitmap(userId, 2026, 4)).thenReturn(0);

        int continuousDays = signComputeSupport.calcCurrentContinuousDays(
                userId,
                today,
                2026,
                4,
                signComputeSupport.markSigned(0, today.getDayOfMonth())
        );

        assertEquals(1, continuousDays);
    }
}
