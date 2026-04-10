package com.yuexiang.user.support;

import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.user.domain.entity.MemberLevel;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.entity.UserWallet;
import com.yuexiang.user.mapper.MemberLevelMapper;
import com.yuexiang.user.mapper.UserMapper;
import com.yuexiang.user.mapper.UserWalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccountQuerySupport {

    private final UserMapper userMapper;
    private final UserWalletMapper userWalletMapper;
    private final MemberLevelMapper memberLevelMapper;

    public User getExistingUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        return user;
    }

    public UserWallet getExistingWallet(Long userId) {
        UserWallet wallet = userWalletMapper.selectById(userId);
        if (wallet == null) {
            throw new NotFoundException("用户钱包不存在");
        }
        return wallet;
    }

    public MemberLevel getLevelByUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.getLevel() == null) {
            return null;
        }
        return memberLevelMapper.selectById(userInfo.getLevel());
    }
}
