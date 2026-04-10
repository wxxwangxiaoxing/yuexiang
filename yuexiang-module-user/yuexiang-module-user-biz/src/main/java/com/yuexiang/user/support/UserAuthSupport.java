package com.yuexiang.user.support;

import com.yuexiang.user.domain.dto.RealNameDTO;
import com.yuexiang.user.domain.entity.UserAuth;
import com.yuexiang.user.domain.enums.RealNameStatusEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserAuthSupport {

    public boolean isPendingOrApproved(Integer status) {
        if (status == null) {
            return false;
        }
        return status == RealNameStatusEnum.AUDITING.getCode()
                || status == RealNameStatusEnum.SUCCESS.getCode();
    }

    public UserAuth buildOrUpdateAuth(UserAuth existAuth, Long userId, RealNameDTO dto) {
        UserAuth auth = existAuth != null ? existAuth : new UserAuth();
        LocalDateTime now = LocalDateTime.now();

        auth.setUserId(userId);
        auth.setRealName(dto.getRealName());
        auth.setIdCard(dto.getIdCard());
        auth.setFrontImage(dto.getFrontImage());
        auth.setBackImage(dto.getBackImage());
        auth.setStatus(RealNameStatusEnum.AUDITING.getCode());
        auth.setUpdateTime(now);

        if (existAuth == null) {
            auth.setCreateTime(now);
        }
        return auth;
    }
}
