package com.yuexiang.user.service.impl;

import com.yuexiang.user.constant.UserAuthConstants;
import com.yuexiang.user.api.UserAuthService;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.entity.UserWallet;
import com.yuexiang.user.mapper.UserInfoMapper;
import com.yuexiang.user.mapper.UserMapper;
import com.yuexiang.user.mapper.UserWalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final UserWalletMapper userWalletMapper;

    @Override
    public User findByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User registerUser(String phone, String nickName, String avatar) {
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setPhone(phone);
        user.setNickName(nickName);
        user.setAvatar(avatar);
        user.setStatus(UserAuthConstants.USER_STATUS_NORMAL);
        user.setDeleted(0);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);

        initUserInfo(user.getId(), now);
        initUserWallet(user.getId(), now);

        log.info("用户注册成功: userId={}, phone={}", user.getId(), phone);
        return user;
    }

    private void initUserInfo(Long userId, LocalDateTime now) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setLevel(1);
        userInfo.setPoints(0);
        userInfo.setFansCount(0);
        userInfo.setFollowCount(0);
        userInfo.setGender(0);
        userInfo.setDeleted(0);
        userInfo.setCreateTime(now);
        userInfo.setUpdateTime(now);
        userInfoMapper.insert(userInfo);
    }

    private void initUserWallet(Long userId, LocalDateTime now) {
        UserWallet wallet = new UserWallet();
        wallet.setUserId(userId);
        wallet.setBalance(0L);
        wallet.setFrozenBalance(0L);
        wallet.setTotalRecharge(0L);
        wallet.setTotalConsume(0L);
        wallet.setVersion(1);
        wallet.setDeleted(0);
        wallet.setCreateTime(now);
        wallet.setUpdateTime(now);
        userWalletMapper.insert(wallet);
    }
}
