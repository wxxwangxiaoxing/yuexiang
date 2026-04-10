package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class BusinessException extends BaseException {

    public BusinessException(ResultCodeEnum codeEnum) {
        super(codeEnum);
    }

    public BusinessException(ResultCodeEnum codeEnum, Object data) {
        super(codeEnum, data);
    }

    public BusinessException(ResultCodeEnum codeEnum, String message) {
        super(codeEnum, message);
    }

    public BusinessException(ResultCodeEnum codeEnum, String message, Object data) {
        super(codeEnum, message, data);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(Integer code, String message, Object data) {
        super(code, message, data);
    }
}
