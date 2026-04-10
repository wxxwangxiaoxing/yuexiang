package com.yuexiang.user.constant;

public final class UserCenterConstants {

    private UserCenterConstants() {
    }

    public static final int VOUCHER_PENDING_PAY = 0;
    public static final int VOUCHER_UNUSED = 1;
    public static final int VOUCHER_USED = 2;
    public static final int VOUCHER_REFUNDED = 3;
    public static final int VOUCHER_CANCELLED = 4;

    public static final int ORDER_PENDING_PAY = 0;
    public static final int ORDER_PAID = 1;
    public static final int ORDER_USED = 2;
    public static final int ORDER_REFUNDED = 3;
    public static final int ORDER_CANCELLED = 4;

    public static final int PAY_NONE = 0;
    public static final int PAY_BALANCE = 1;
    public static final int PAY_WECHAT = 2;
    public static final int PAY_ALIPAY = 3;

    public static final int BLOG_PENDING = 0;
    public static final int BLOG_PUBLISHED = 1;
    public static final int BLOG_BLOCKED = 2;
    public static final int BLOG_DRAFT = 3;

    public static final int BIZ_TYPE_SHOP = 1;
}
