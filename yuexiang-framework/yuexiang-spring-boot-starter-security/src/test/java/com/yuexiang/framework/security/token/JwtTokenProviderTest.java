package com.yuexiang.framework.security.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.framework.security.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-must-be-at-least-32-characters-long");
        properties.setAccessTokenExpire(7200);
        properties.setRefreshTokenExpire(604800);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        jwtTokenProvider = new JwtTokenProvider(properties, redisTemplate, new ObjectMapper());
    }

    @Test
    void generateTokenPairCreatesCompleteTokenSet() {
        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(1001L);

        assertNotNull(tokenPair);
        assertNotNull(tokenPair.getAccessToken());
        assertNotNull(tokenPair.getRefreshToken());
        assertEquals(7200, tokenPair.getExpiresIn());
        assertEquals(604800, tokenPair.getAbsoluteExpiresIn());
        assertNotNull(tokenPair.getAccessJti());
        assertNotNull(tokenPair.getRefreshJti());
        assertNotNull(tokenPair.getFamilyId());

        verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any());
        verify(setOperations).add(anyString(), anyString(), anyString());
    }

    @Test
    void parseTokenReturnsClaimsFromAccessToken() {
        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(1001L);

        var decoded = jwtTokenProvider.parseToken(tokenPair.getAccessToken());

        assertNotNull(decoded);
        assertEquals("access", decoded.getClaim("typ").asString());
        assertEquals(1001L, decoded.getClaim("uid").asLong());
    }

    @Test
    void validateAccessTokenReturnsFalseWhenBlacklisted() {
        TokenPair tokenPair = jwtTokenProvider.generateTokenPair(1001L);
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        boolean valid = jwtTokenProvider.validateAccessToken(tokenPair.getAccessToken());

        assertFalse(valid);
    }
}
