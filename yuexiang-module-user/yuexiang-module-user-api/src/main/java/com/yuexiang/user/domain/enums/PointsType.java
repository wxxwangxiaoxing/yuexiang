package com.yuexiang.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointsType {

    EARN(3, "获取"),
    DEDUCT(5, "扣减");

    private final int code;
    private final String desc;
}