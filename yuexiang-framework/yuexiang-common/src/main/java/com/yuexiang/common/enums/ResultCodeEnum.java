package com.yuexiang.common.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SHOP_NOT_FOUND(40401, "店铺不存在"),
    SHOP_UPDATE_FAIL(50001, "店铺更新失败"),
    SHOP_QUERY_FAIL(50002, "店铺查询失败"),


    SUCCESS(200, "成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    REQUEST_TIMEOUT(408, "请求超时"),

    INTERNAL_ERROR(500, "系统异常，请联系管理员"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    ACCOUNT_LOCKED(1004, "账户已被锁定"),
    INVALID_TOKEN(1005, "Token无效"),
    TOKEN_EXPIRED(1006, "Token已过期"),
    RESOURCE_CONFLICT(1007, "资源冲突"),
    CAPTCHA_ERROR(1008, "验证码错误"),
    DUPLICATE_REQUEST(1009, "重复请求，请稍后重试"),
    
    // ==================== 积分相关错误 ====================
    POINTS_INVALID(2001, "积分值无效"),
    POINTS_INSUFFICIENT(2002, "积分不足"),
    POINTS_ADD_FAILED(2003, "积分增加失败"),
    POINTS_DEDUCT_FAILED(2004, "积分扣减失败"),
    USER_INFO_INIT_FAILED(2005, "用户信息初始化失败"),
    POINTS_RECORD_SAVE_FAILED(2006, "积分流水记录保存失败"),
    LEVEL_UPGRADE_FAILED(2007, "用户等级升级失败");

    // ==================== 业务错误 ====================

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
