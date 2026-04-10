package com.yuexiang.user.domain.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    GenderEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(int code) {
        for (GenderEnum e : values()) {
            if (e.code == code) return e.desc;
        }
        return UNKNOWN.desc;
    }
}