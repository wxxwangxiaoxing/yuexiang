package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super(ResultCodeEnum.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ResultCodeEnum.UNAUTHORIZED, message);
    }

    public UnauthorizedException(Object data) {
        super(ResultCodeEnum.UNAUTHORIZED, data);
    }

    public UnauthorizedException(String message, Object data) {
        super(ResultCodeEnum.UNAUTHORIZED, message, data);
    }
}
