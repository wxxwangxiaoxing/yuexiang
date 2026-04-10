package com.yuexiang.user.support;

import com.yuexiang.common.config.BusinessProperties;
import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.domain.entity.MemberLevel;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.mapper.MemberLevelMapper;
import com.yuexiang.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPointsAccountSupport {

    private final UserInfoMapper userInfoMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final BusinessProperties businessProperties;

    public UserInfo getOrCreateUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo != null) {
            return userInfo;
        }

        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setPoints(businessProperties.getPoints().getMinPoints());
        userInfo.setLevel(businessProperties.getPoints().getDefaultLevel());

        try {
            userInfoMapper.insert(userInfo);
            log.info("[初始化用户信息成功] userId={}", userId);
            return userInfo;
        } catch (DuplicateKeyException e) {
            log.info("[并发插入冲突，重新查询] userId={}", userId);
            userInfo = userInfoMapper.selectById(userId);
            if (userInfo == null) {
                log.error("[初始化用户信息失败] userId={}", userId);
                throw new BusinessException(ResultCodeEnum.USER_INFO_INIT_FAILED,
                        String.format("初始化用户信息失败，用户ID: %d", userId), e);
            }
            return userInfo;
        } catch (Exception e) {
            log.error("[初始化用户信息异常] userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.USER_INFO_INIT_FAILED,
                    String.format("初始化用户信息失败，用户ID: %d", userId), e);
        }
    }

    public boolean checkAndUpgradeLevel(Long userId, Integer currentPoints, Integer currentLevel) {
        try {
            MemberLevel newLevel = memberLevelMapper.selectByPoints(currentPoints);
            if (newLevel == null) {
                log.warn("[等级查询失败] 未找到对应等级配置: points={}", currentPoints);
                return false;
            }

            if (Objects.equals(newLevel.getLevel(), currentLevel)) {
                return false;
            }

            int affectedRows = userInfoMapper.updateLevel(userId, newLevel.getLevel());
            if (affectedRows == 0) {
                log.warn("[等级更新失败] userId={}, level={}", userId, newLevel.getLevel());
                return false;
            }

            log.info("[用户等级变更成功] userId={}, {}->{}", userId, currentLevel, newLevel.getLevel());
            return true;
        } catch (Exception e) {
            log.error("[等级变更检查异常] userId={}, currentPoints={}, currentLevel={}, error={}",
                    userId, currentPoints, currentLevel, e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.LEVEL_UPGRADE_FAILED,
                    "等级变更检查过程中发生系统异常", e);
        }
    }
}
