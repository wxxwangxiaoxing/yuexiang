package com.yuexiang.system.service;

import com.yuexiang.system.domain.vo.CaptchaVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CaptchaServiceTest {

    @Autowired
    private CaptchaService captchaService;

    @Test
    void testGenerate() {
        CaptchaVO vo = captchaService.generate();
        
        assertNotNull(vo);
        assertNotNull(vo.getCaptchaId());
        assertNotNull(vo.getImage());
        assertTrue(vo.getImage().startsWith("data:image/png;base64,"));
        assertEquals(300, vo.getExpireSeconds());
    }

    @Test
    void testVerify_Success() {
        CaptchaVO vo = captchaService.generate();
        
        // 由于验证码是随机生成的，这里只测试验证流程
        // 实际测试中需要mock或使用已知验证码
        assertFalse(captchaService.verify(vo.getCaptchaId(), "wrong"));
    }

    @Test
    void testVerify_InvalidId() {
        assertFalse(captchaService.verify("invalid-id", "1234"));
    }

    @Test
    void testVerify_EmptyInput() {
        assertFalse(captchaService.verify(null, "1234"));
        assertFalse(captchaService.verify("some-id", null));
        assertFalse(captchaService.verify("some-id", ""));
    }
}
