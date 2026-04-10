package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class NotFoundException extends BaseException {

    public NotFoundException() {
        super(ResultCodeEnum.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(ResultCodeEnum.NOT_FOUND, message);
    }

    public NotFoundException(Object data) {
        super(ResultCodeEnum.NOT_FOUND, data);
    }

    public NotFoundException(String message, Object data) {
        super(ResultCodeEnum.NOT_FOUND, message, data);
    }
}
