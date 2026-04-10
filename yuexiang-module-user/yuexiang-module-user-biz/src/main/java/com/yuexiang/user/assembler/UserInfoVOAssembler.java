package com.yuexiang.user.assembler;

import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.vo.UserInfoVO;
import org.springframework.stereotype.Component;

@Component
public class UserInfoVOAssembler {

    public UserInfoVO assemble(Long userId, UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }
        return UserInfoVO.builder()
                .userId(userId)
                .points(userInfo.getPoints())
                .level(userInfo.getLevel())
                .build();
    }
}
