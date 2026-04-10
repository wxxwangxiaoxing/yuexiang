package com.yuexiang.system.service;

import com.yuexiang.system.domain.vo.CaptchaVO;

public interface CaptchaService {

    CaptchaVO generate();

    boolean verify(String captchaId, String input);
}
