package com.yuexiang.system.constant;

public final class SmsCodeConstants {

    private SmsCodeConstants() {
    }

    public static final int TYPE_LOGIN = 0;
    public static final int TYPE_REGISTER = 1;
    public static final int TYPE_RESET_PWD = 2;

    public static final int STATUS_UNUSED = 0;
    public static final int STATUS_USED = 1;
    public static final int STATUS_EXPIRED = 2;
}
