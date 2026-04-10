package com.yuexiang.user.assembler;

import com.yuexiang.user.domain.entity.UserInfo;

public record SafeUserInfo(UserInfo info) {

    public static SafeUserInfo of(UserInfo info) {
        return new SafeUserInfo(info);
    }

    public int getGender() {
        return info != null && info.getGender() != null ? info.getGender() : 0;
    }

    public String getCity() {
        return info != null ? info.getCity() : null;
    }

    public String getIntroduce() {
        return info != null ? info.getIntroduce() : null;
    }

    public int getLevel() {
        return info != null && info.getLevel() != null ? info.getLevel() : 1;
    }

    public int getPoints() {
        return info != null && info.getPoints() != null ? info.getPoints() : 0;
    }

    public int getFollowCount() {
        return info != null && info.getFollowCount() != null ? info.getFollowCount() : 0;
    }

    public int getFansCount() {
        return info != null && info.getFansCount() != null ? info.getFansCount() : 0;
    }

    public int getLikeCount() {
        return info != null && info.getLikeCount() != null ? info.getLikeCount() : 0;
    }
}
