package com.yuexiang.system.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void testIsValidPhone_Chinese() {
        assertTrue(ValidationUtil.isValidPhone("+8613800138000"));
        assertTrue(ValidationUtil.isValidPhone("+8618812345678"));
    }

    @Test
    void testIsValidPhone_International() {
        assertTrue(ValidationUtil.isValidPhone("+12025551234"));
        assertTrue(ValidationUtil.isValidPhone("+85212345678"));
    }

    @Test
    void testIsValidPhone_Invalid() {
        assertFalse(ValidationUtil.isValidPhone(null));
        assertFalse(ValidationUtil.isValidPhone(""));
        assertFalse(ValidationUtil.isValidPhone("13800138000"));
        assertFalse(ValidationUtil.isValidPhone("+8612345678"));
        assertFalse(ValidationUtil.isValidPhone("+123"));
    }

    @Test
    void testIsCnPhone() {
        assertTrue(ValidationUtil.isCnPhone("+8613800138000"));
        assertTrue(ValidationUtil.isCnPhone("+8619912345678"));
        assertFalse(ValidationUtil.isCnPhone("+12025551234"));
        assertFalse(ValidationUtil.isCnPhone("+8612345678901"));
    }

    @Test
    void testIsValidPassword() {
        assertTrue(ValidationUtil.isValidPassword("Abc12345"));
        assertTrue(ValidationUtil.isValidPassword("Password1"));
        assertTrue(ValidationUtil.isValidPassword("Test1234"));
        assertTrue(ValidationUtil.isValidPassword("Abc123!@#"));
    }

    @Test
    void testIsValidPassword_Invalid() {
        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword("abc12345"));
        assertFalse(ValidationUtil.isValidPassword("ABC12345"));
        assertFalse(ValidationUtil.isValidPassword("Abcdefgh"));
        assertFalse(ValidationUtil.isValidPassword("Abc12"));
        assertFalse(ValidationUtil.isValidPassword("Abc12345678901234567890"));
    }

    @Test
    void testIsWeakPassword() {
        assertTrue(ValidationUtil.isWeakPassword("password"));
        assertTrue(ValidationUtil.isWeakPassword("123456"));
        assertTrue(ValidationUtil.isWeakPassword("Password1"));
        assertTrue(ValidationUtil.isWeakPassword("Admin123"));
        
        assertFalse(ValidationUtil.isWeakPassword("MySecure123"));
        assertFalse(ValidationUtil.isWeakPassword("ComplexP@ss1"));
    }
}
