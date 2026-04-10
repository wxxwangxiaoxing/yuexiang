package com.yuexiang.user.support;

import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.user.domain.entity.MemberLevel;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.mapper.MemberLevelMapper;
import com.yuexiang.user.mapper.UserInfoMapper;
import com.yuexiang.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileQuerySupport {

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final MemberLevelMapper memberLevelMapper;

    public User getExistingUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        return user;
    }

    public UserInfo getOrDefaultUserInfo(Long userId) {
        UserInfo info = userInfoMapper.selectById(userId);
        if (info != null) {
            return info;
        }

        UserInfo def = new UserInfo();
        def.setUserId(userId);
        def.setLevel(1);
        def.setPoints(0);
        def.setFollowCount(0);
        def.setFansCount(0);
        def.setLikeCount(0);
        def.setGender(0);
        return def;
    }

    public MemberLevel getLevel(Integer level) {
        return level != null ? memberLevelMapper.selectById(level) : null;
    }
}
