package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class DuplicateRequestException extends BaseException {

    public DuplicateRequestException() {
        super(ResultCodeEnum.DUPLICATE_REQUEST);
    }

    public DuplicateRequestException(String message) {
        super(ResultCodeEnum.DUPLICATE_REQUEST, message);
    }

    public DuplicateRequestException(Object data) {
        super(ResultCodeEnum.DUPLICATE_REQUEST, data);
    }

    public DuplicateRequestException(String message, Object data) {
        super(ResultCodeEnum.DUPLICATE_REQUEST, message, data);
    }
}
