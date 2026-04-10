package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class AccountLockedException extends BaseException {

    public AccountLockedException() {
        super(ResultCodeEnum.ACCOUNT_LOCKED);
    }

    public AccountLockedException(String message) {
        super(ResultCodeEnum.ACCOUNT_LOCKED, message);
    }

    public AccountLockedException(Object data) {
        super(ResultCodeEnum.ACCOUNT_LOCKED, data);
    }

    public AccountLockedException(String message, Object data) {
        super(ResultCodeEnum.ACCOUNT_LOCKED, message, data);
    }
}
