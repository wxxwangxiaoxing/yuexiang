package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;

/**
 * 地理位置服务不可用异常
 * 用于 LBS 查询中 Redis 和 DB 均失败的兜底报错
 */
public class GeoServiceUnavailableException extends BaseException {

    // 默认构造函数：使用通用的服务异常枚举（假设你的 ResultCodeEnum 中有相应定义）
    public GeoServiceUnavailableException() {
        super(ResultCodeEnum.SERVICE_UNAVAILABLE, "位置服务暂时不可用，请稍后重试");
    }

    // 支持自定义消息的构造函数
    public GeoServiceUnavailableException(String message) {
        super(ResultCodeEnum.SERVICE_UNAVAILABLE, message);
    }

    // 支持完整参数的构造函数（枚举 + 自定义消息）
    public GeoServiceUnavailableException(ResultCodeEnum codeEnum, String message) {
        super(codeEnum, message);
    }

    // 支持携带额外数据的构造函数
    public GeoServiceUnavailableException(ResultCodeEnum codeEnum, String message, Object data) {
        super(codeEnum, message, data);
    }

    // 如果没有合适的枚举，支持直接传入错误码
    public GeoServiceUnavailableException(Integer code, String message) {
        super(code, message);
    }
}