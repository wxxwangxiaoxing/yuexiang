package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class TokenExpiredException extends BaseException {

    public TokenExpiredException() {
        super(ResultCodeEnum.TOKEN_EXPIRED);
    }

    public TokenExpiredException(String message) {
        super(ResultCodeEnum.TOKEN_EXPIRED, message);
    }

    public TokenExpiredException(Object data) {
        super(ResultCodeEnum.TOKEN_EXPIRED, data);
    }

    public TokenExpiredException(String message, Object data) {
        super(ResultCodeEnum.TOKEN_EXPIRED, message, data);
    }
}
