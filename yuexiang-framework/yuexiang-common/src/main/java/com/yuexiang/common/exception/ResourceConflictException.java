package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class ResourceConflictException extends BaseException {

    public ResourceConflictException() {
        super(ResultCodeEnum.RESOURCE_CONFLICT);
    }

    public ResourceConflictException(String message) {
        super(ResultCodeEnum.RESOURCE_CONFLICT, message);
    }

    public ResourceConflictException(Object data) {
        super(ResultCodeEnum.RESOURCE_CONFLICT, data);
    }

    public ResourceConflictException(String message, Object data) {
        super(ResultCodeEnum.RESOURCE_CONFLICT, message, data);
    }
}
