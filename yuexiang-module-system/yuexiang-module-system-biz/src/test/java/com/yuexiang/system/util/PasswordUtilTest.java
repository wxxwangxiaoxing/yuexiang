package com.yuexiang.system.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void testEncode() {
        String rawPassword = "Abc12345";
        String encoded = PasswordUtil.encode(rawPassword);
        
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$10$"));
        assertNotEquals(rawPassword, encoded);
    }

    @Test
    void testMatches_Success() {
        String rawPassword = "Abc12345";
        String encoded = PasswordUtil.encode(rawPassword);
        
        assertTrue(PasswordUtil.matches(rawPassword, encoded));
    }

    @Test
    void testMatches_Failure() {
        String rawPassword = "Abc12345";
        String encoded = PasswordUtil.encode(rawPassword);
        
        assertFalse(PasswordUtil.matches("Wrong123", encoded));
    }

    @Test
    void testMatches_Null() {
        assertFalse(PasswordUtil.matches(null, "$2a$10$xxxxx"));
        assertFalse(PasswordUtil.matches("Abc12345", null));
    }

    @Test
    void testGenerateNickname() {
        String nickname = PasswordUtil.generateNickname();
        
        assertNotNull(nickname);
        assertTrue(nickname.startsWith("user_"));
        assertEquals(11, nickname.length());
    }

    @Test
    void testGenerateSmsCode() {
        String code = PasswordUtil.generateSmsCode();
        
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }
}
