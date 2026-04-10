package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(ResultCodeEnum.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ResultCodeEnum.FORBIDDEN, message);
    }

    public ForbiddenException(Object data) {
        super(ResultCodeEnum.FORBIDDEN, data);
    }

    public ForbiddenException(String message, Object data) {
        super(ResultCodeEnum.FORBIDDEN, message, data);
    }
}
