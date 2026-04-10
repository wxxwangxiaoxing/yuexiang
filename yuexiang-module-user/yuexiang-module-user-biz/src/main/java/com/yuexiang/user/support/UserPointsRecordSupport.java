package com.yuexiang.user.support;

import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.domain.entity.PointsRecord;
import com.yuexiang.user.domain.enums.PointsType;
import com.yuexiang.user.mapper.PointsRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPointsRecordSupport {

    private final PointsRecordMapper pointsRecordMapper;

    public void savePointsRecord(Long userId, PointsType type, Integer points,
                                 Integer balance, String bizType,
                                 Long bizId, String description) {
        try {
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setType(type.getCode());
            record.setPoints(points);
            record.setBalance(balance);
            record.setBizType(bizType);
            record.setBizId(bizId);
            record.setDescription(description);
            pointsRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("[保存积分流水失败] userId={}, type={}, points={}, error={}",
                    userId, type, points, e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.POINTS_RECORD_SAVE_FAILED,
                    "保存积分流水记录失败", e);
        }
    }
}
