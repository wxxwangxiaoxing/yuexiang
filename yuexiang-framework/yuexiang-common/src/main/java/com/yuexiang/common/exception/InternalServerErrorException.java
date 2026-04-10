package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class InternalServerErrorException extends BaseException {

    public InternalServerErrorException() {
        super(ResultCodeEnum.INTERNAL_ERROR);
    }

    public InternalServerErrorException(String message) {
        super(ResultCodeEnum.INTERNAL_ERROR, message);
    }

    public InternalServerErrorException(Object data) {
        super(ResultCodeEnum.INTERNAL_ERROR, data);
    }

    public InternalServerErrorException(String message, Object data) {
        super(ResultCodeEnum.INTERNAL_ERROR, message, data);
    }
}
