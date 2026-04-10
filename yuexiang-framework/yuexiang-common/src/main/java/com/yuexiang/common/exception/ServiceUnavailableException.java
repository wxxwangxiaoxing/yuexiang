package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class ServiceUnavailableException extends BaseException {

    public ServiceUnavailableException() {
        super(ResultCodeEnum.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String message) {
        super(ResultCodeEnum.SERVICE_UNAVAILABLE, message);
    }

    public ServiceUnavailableException(Object data) {
        super(ResultCodeEnum.SERVICE_UNAVAILABLE, data);
    }

    public ServiceUnavailableException(String message, Object data) {
        super(ResultCodeEnum.SERVICE_UNAVAILABLE, message, data);
    }
}
