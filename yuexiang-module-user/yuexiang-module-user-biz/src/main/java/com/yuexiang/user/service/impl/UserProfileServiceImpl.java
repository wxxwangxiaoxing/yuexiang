package com.yuexiang.user.service.impl;

import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.user.assembler.UserProfileAssembler;
import com.yuexiang.user.constant.UserProfileConstants;
import com.yuexiang.user.domain.entity.MemberLevel;
import com.yuexiang.user.domain.entity.Sign;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.vo.SignCalendarVO;
import com.yuexiang.user.domain.vo.SignResultVO;
import com.yuexiang.user.domain.vo.UserProfileVO;
import com.yuexiang.user.mapper.SignMapper;
import com.yuexiang.user.mapper.UserInfoMapper;
import com.yuexiang.user.api.UserProfileService;
import com.yuexiang.user.support.UserProfileQuerySupport;
import com.yuexiang.user.support.UserProfileSignSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserInfoMapper userInfoMapper;
    private final SignMapper signMapper;
    private final UserProfileQuerySupport userProfileQuerySupport;
    private final UserProfileSignSupport userProfileSignSupport;
    private final UserProfileAssembler userProfileAssembler;

    // ==================== 个人主页 ====================

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userProfileQuerySupport.getExistingUser(userId);
        UserInfo userInfo = userProfileQuerySupport.getOrDefaultUserInfo(userId);
        MemberLevel level = userProfileQuerySupport.getLevel(userInfo.getLevel());

        LocalDate today = LocalDate.now();
        Sign currentSign = userProfileSignSupport.getUserMonthSign(userId, today.getYear(), today.getMonthValue());
        boolean signedToday = userProfileSignSupport.isBitSetSafe(currentSign, today.getDayOfMonth());
        int continuousDays = userProfileSignSupport.calcContinuousDays(userId, today, signedToday, currentSign);

        return userProfileAssembler.assemble(userId, user, userInfo, level, signedToday, continuousDays);
    }

    // ==================== 签到日历 ====================

    @Override
    public SignCalendarVO getSignCalendar(Long userId, Integer year, Integer month) {
        LocalDate today = LocalDate.now();
        int qYear  = year  != null ? year  : today.getYear();
        int qMonth = month != null ? month : today.getMonthValue();
        boolean isCurrentMonth =
                qYear == today.getYear() && qMonth == today.getMonthValue();

        Sign sign = userProfileSignSupport.getUserMonthSign(userId, qYear, qMonth);

        List<Integer> signedDays = sign != null
                ? userProfileSignSupport.parseBitmap(sign.getSignBitmap()) : List.of();
        int signedCount = sign != null ? sign.getSignDays() : 0;
        boolean signedToday = isCurrentMonth
                && userProfileSignSupport.isBitSetSafe(sign, today.getDayOfMonth());

        int continuousDays = userProfileSignSupport.calcContinuousDays(
                userId, today, signedToday, isCurrentMonth ? sign : null);

        // 积分预览
        int todayReward, tomorrowReward;
        if (signedToday) {
            todayReward = userProfileSignSupport.calculatePoints(continuousDays);
            tomorrowReward = userProfileSignSupport.calculatePoints(continuousDays + 1);
        } else {
            todayReward = userProfileSignSupport.calculatePoints(continuousDays + 1);
            tomorrowReward = userProfileSignSupport.calculatePoints(continuousDays + 2);
        }

        LocalDate monthStart = LocalDate.of(qYear, qMonth, 1);

        return SignCalendarVO.builder()
                .year(qYear)
                .month(qMonth)
                .today(isCurrentMonth ? today.getDayOfMonth() : null)
                .totalDaysInMonth(monthStart.lengthOfMonth())
                .firstDayOfWeek(monthStart.getDayOfWeek().getValue())
                .signedDays(signedDays)
                .signedCount(signedCount)
                .continuousDays(continuousDays)
                .isSignedToday(signedToday)
                .todayPoints(todayReward)
                .monthTotalPoints(userProfileSignSupport.calculateMonthTotalPoints(sign))
                .serverTime(System.currentTimeMillis())
                .build();
    }

    // ==================== 签到 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignResultVO sign(Long userId) {
        LocalDate today = LocalDate.now();
        int year  = today.getYear();
        int month = today.getMonthValue();
        int day   = today.getDayOfMonth();

        // ⚠️ 高并发场景建议 mapper 改用 SELECT ... FOR UPDATE
        Sign sign = userProfileSignSupport.getOrCreateMonthSign(userId, year, month);
        long bitmap = sign.getSignBitmap();

        if (userProfileSignSupport.isBitSet(bitmap, day)) {
            throw new BadRequestException("今日已签到，明天再来吧");
        }

        int prevContinuous = userProfileSignSupport.calcContinuousDays(userId, today, false, sign);
        int currContinuous = prevContinuous + 1;
        int basePoints = userProfileSignSupport.calculatePoints(currContinuous);

        boolean isBonus    = currContinuous % UserProfileConstants.BONUS_TRIGGER_DAYS == 0;
        int bonusPoints    = isBonus ? UserProfileConstants.BONUS_POINTS : 0;
        int totalReward    = basePoints + bonusPoints;

        userInfoMapper.addPoints(userId, totalReward);
        long newBitmap = bitmap | (1L << (day - 1));
        signMapper.updateBitmap(sign.getId(), newBitmap, sign.getSignDays() + 1);

        UserInfo updatedUser = userInfoMapper.selectById(userId);

        log.info("用户签到成功: userId={}, base={}, bonus={}, 连续{}天",
                userId, basePoints, bonusPoints, currContinuous);

        return SignResultVO.builder()
                .signDate(today.format(UserProfileConstants.DATE_FORMAT))
                .day(day)
                .basePoints(basePoints)
                .bonusPoints(bonusPoints)
                .totalRewardPoints(totalReward)
                .userTotalPoints(updatedUser.getPoints())
                .continuousDays(currContinuous)
                .isBonus(isBonus)
                .bonusDesc(isBonus
                        ? String.format("🎉 连续签到%d天，额外奖励%d积分！",
                        UserProfileConstants.BONUS_TRIGGER_DAYS, UserProfileConstants.BONUS_POINTS)
                        : null)
                .build();
    }
}
