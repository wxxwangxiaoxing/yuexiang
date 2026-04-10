package com.yuexiang.user.support;

import com.yuexiang.common.config.BusinessProperties;
import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPointsValidationSupport {

    private final BusinessProperties businessProperties;

    public void validateParams(Long userId, Integer points) {
        if (userId == null) {
            log.warn("[参数校验失败] userId为空");
            throw new BadRequestException("用户ID不能为空");
        }
        if (points == null) {
            log.warn("[参数校验失败] points为空, userId={}", userId);
            throw new BadRequestException("积分值不能为空");
        }
        if (points <= 0) {
            log.warn("[参数校验失败] 积分值无效: points={}, userId={}", points, userId);
            throw new BusinessException(ResultCodeEnum.POINTS_INVALID,
                    String.format("积分值必须大于0，当前值: %d", points));
        }
    }

    public void validatePointsRange(Integer points) {
        if (points > businessProperties.getPoints().getMaxPoints()) {
            log.warn("[积分范围校验失败] 积分值超出最大限制: points={}", points);
            throw new BusinessException(ResultCodeEnum.POINTS_INVALID,
                    String.format("积分值超出最大限制，最大值: %d, 当前值: %d",
                            businessProperties.getPoints().getMaxPoints(), points));
        }
    }
}
