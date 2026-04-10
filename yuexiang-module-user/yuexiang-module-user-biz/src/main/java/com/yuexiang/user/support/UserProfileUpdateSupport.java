package com.yuexiang.user.support;

import com.yuexiang.user.domain.dto.UpdateUserInfoDTO;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.mapper.UserInfoMapper;
import com.yuexiang.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserProfileUpdateSupport {

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final UserAccountQuerySupport userAccountQuerySupport;

    public void updateUserBasicInfo(Long userId, UpdateUserInfoDTO dto) {
        if (!StringUtils.hasText(dto.getNickName())) {
            return;
        }

        User user = userAccountQuerySupport.getExistingUser(userId);
        if (!dto.getNickName().equals(user.getNickName())) {
            user.setNickName(dto.getNickName());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    public void updateUserExtendInfo(Long userId, UpdateUserInfoDTO dto) {
        boolean hasExtendUpdate = dto.getGender() != null
                || dto.getBirthday() != null
                || StringUtils.hasText(dto.getCity())
                || StringUtils.hasText(dto.getIntroduce());

        if (!hasExtendUpdate) {
            return;
        }

        UserInfo userInfo = userInfoMapper.selectById(userId);
        boolean isNew = userInfo == null;
        if (isNew) {
            userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setLevel(1);
            userInfo.setPoints(0);
            userInfo.setCreateTime(LocalDateTime.now());
        }

        if (dto.getGender() != null) {
            userInfo.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            userInfo.setBirthday(dto.getBirthday());
        }
        if (StringUtils.hasText(dto.getCity())) {
            userInfo.setCity(dto.getCity());
        }
        if (StringUtils.hasText(dto.getIntroduce())) {
            userInfo.setIntroduce(dto.getIntroduce());
        }
        userInfo.setUpdateTime(LocalDateTime.now());

        if (isNew) {
            userInfoMapper.insert(userInfo);
        } else {
            userInfoMapper.updateById(userInfo);
        }
    }
}
