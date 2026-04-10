package com.yuexiang.common.exception;

import com.yuexiang.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class CaptchaErrorException extends BaseException {

    public CaptchaErrorException() {
        super(ResultCodeEnum.CAPTCHA_ERROR);
    }

    public CaptchaErrorException(String message) {
        super(ResultCodeEnum.CAPTCHA_ERROR, message);
    }

    public CaptchaErrorException(Object data) {
        super(ResultCodeEnum.CAPTCHA_ERROR, data);
    }

    public CaptchaErrorException(String message, Object data) {
        super(ResultCodeEnum.CAPTCHA_ERROR, message, data);
    }
}
