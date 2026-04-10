package com.yuexiang.user.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.user.constant.SignConstants;
import com.yuexiang.user.domain.entity.Sign;
import com.yuexiang.user.domain.entity.SignRepairRecord;
import com.yuexiang.user.domain.entity.SignRewardRule;
import com.yuexiang.user.mapper.SignMapper;
import com.yuexiang.user.mapper.SignRepairRecordMapper;
import com.yuexiang.user.mapper.SignRewardRuleMapper;
import com.yuexiang.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SignQuerySupport {

    private final SignMapper signMapper;
    private final SignRewardRuleMapper rewardRuleMapper;
    private final SignRepairRecordMapper repairRecordMapper;
    private final UserInfoMapper userInfoMapper;

    public Sign getUserMonthSign(Long userId, int year, int month) {
        return signMapper.selectByUserMonth(userId, year, month);
    }

    public Sign getOrCreateSign(Long userId, int year, int month) {
        Sign sign = signMapper.selectByUserMonth(userId, year, month);
        if (sign != null) {
            return sign;
        }

        Sign newSign = new Sign();
        newSign.setUserId(userId);
        newSign.setYear(year);
        newSign.setMonth(month);
        newSign.setSignBitmap(0);
        newSign.setSignDays(0);
        newSign.setDeleted(SignConstants.NOT_DELETED);
        signMapper.insert(newSign);
        return newSign;
    }

    public List<Integer> getRepairedDays(Long userId, int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

        LambdaQueryWrapper<SignRepairRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignRepairRecord::getUserId, userId)
                .ge(SignRepairRecord::getRepairDate, firstDay)
                .le(SignRepairRecord::getRepairDate, lastDay);

        return repairRecordMapper.selectList(wrapper).stream()
                .map(record -> record.getRepairDate().getDayOfMonth())
                .toList();
    }

    public List<SignRewardRule> getEnabledRewardRules() {
        return rewardRuleMapper.selectAllEnabled();
    }

    public SignRewardRule getRewardRule(Long ruleId) {
        return rewardRuleMapper.selectById(ruleId);
    }

    public int getMaxContinuousSignDays(Long userId) {
        Integer maxContinuousDays = userInfoMapper.getMaxContinuousSignDays(userId);
        return maxContinuousDays != null ? maxContinuousDays : 0;
    }

    public boolean isSignedToday(Long userId, LocalDate today) {
        Sign sign = signMapper.selectByUserMonth(userId, today.getYear(), today.getMonthValue());
        return sign != null && isSigned(sign.getSignBitmap(), today.getDayOfMonth());
    }

    private boolean isSigned(int bitmap, int day) {
        return ((bitmap >>> (day - 1)) & 1) == 1;
    }
}
