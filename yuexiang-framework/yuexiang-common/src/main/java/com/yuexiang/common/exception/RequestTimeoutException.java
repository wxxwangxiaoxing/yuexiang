package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class RequestTimeoutException extends BaseException {

    public RequestTimeoutException() {
        super(ResultCodeEnum.REQUEST_TIMEOUT);
    }

    public RequestTimeoutException(String message) {
        super(ResultCodeEnum.REQUEST_TIMEOUT, message);
    }

    public RequestTimeoutException(Object data) {
        super(ResultCodeEnum.REQUEST_TIMEOUT, data);
    }

    public RequestTimeoutException(String message, Object data) {
        super(ResultCodeEnum.REQUEST_TIMEOUT, message, data);
    }
}
