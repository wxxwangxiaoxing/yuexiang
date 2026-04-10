package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class InvalidTokenException extends BaseException {

    public InvalidTokenException() {
        super(ResultCodeEnum.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(ResultCodeEnum.INVALID_TOKEN, message);
    }

    public InvalidTokenException(Object data) {
        super(ResultCodeEnum.INVALID_TOKEN, data);
    }

    public InvalidTokenException(String message, Object data) {
        super(ResultCodeEnum.INVALID_TOKEN, message, data);
    }
}
