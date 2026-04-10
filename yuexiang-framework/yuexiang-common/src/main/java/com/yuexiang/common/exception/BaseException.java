package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final Integer code;
    private final String message;
    private final transient Object data;

    protected BaseException(ResultCodeEnum codeEnum) {
        this(codeEnum, null, null);
    }

    protected BaseException(ResultCodeEnum codeEnum, Object data) {
        this(codeEnum, null, data);
    }

    protected BaseException(ResultCodeEnum codeEnum, String message) {
        this(codeEnum, message, null);
    }

    protected BaseException(ResultCodeEnum codeEnum, String message, Object data) {
        super(buildMessage(codeEnum.getMsg(), message));
        this.code = codeEnum.getCode();
        this.message = hasText(message) ? message : codeEnum.getMsg();
        this.data = data;
    }

    protected BaseException(Integer code, String message) {
        this(code, message, null);
    }

    protected BaseException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private static String buildMessage(String defaultMsg, String overrideMsg) {
        return hasText(overrideMsg) ? overrideMsg : defaultMsg;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public synchronized BaseException fillInStackTrace() {
        return this;
    }
}
