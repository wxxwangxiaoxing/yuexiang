package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(ResultCodeEnum.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(ResultCodeEnum.BAD_REQUEST, message);
    }

    public BadRequestException(Object data) {
        super(ResultCodeEnum.BAD_REQUEST, data);
    }

    public BadRequestException(String message, Object data) {
        super(ResultCodeEnum.BAD_REQUEST, message, data);
    }
}
