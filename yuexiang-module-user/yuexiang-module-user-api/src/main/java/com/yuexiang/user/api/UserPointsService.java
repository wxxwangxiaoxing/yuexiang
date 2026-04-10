package com.yuexiang.user.api;

import com.yuexiang.user.domain.vo.UserInfoVO;

public interface UserPointsService {

    boolean addPoints(Long userId, Integer points, String bizType, Long bizId, String description);

    boolean deductPoints(Long userId, Integer points, String bizType, Long bizId, String description);

    boolean checkAndUpgradeLevel(Long userId);

    UserInfoVO getUserInfo(Long userId);
}
