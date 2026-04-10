package com.yuexiang.user.domain.enums;

import lombok.Getter;

@Getter
public enum RealNameStatusEnum {
    NOT_SUBMITTED(-1, "未提交"),
    AUDITING(0, "审核中"),
    SUCCESS(1, "已认证"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    RealNameStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(int code) {
        for (RealNameStatusEnum e : values()) {
            if (e.code == code) return e.desc;
        }
        return NOT_SUBMITTED.desc;
    }
}