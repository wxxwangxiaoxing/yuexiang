package com.yuexiang.framework.security.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedissonClient redissonClient;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor(stringRedisTemplate, redissonClient, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void preHandleWithoutAuthorizationHeaderSkipsAuthentication() throws Exception {
        boolean result = authInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertTrue(result);
        assertNull(UserContext.get());
    }

    @Test
    void preHandleLoadsLoginUserFromRedisToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:access-token"))
                .thenReturn(new ObjectMapper().writeValueAsString(new LoginUser(1001L, "tester")));

        boolean result = authInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(result);
        assertNotNull(UserContext.get());
        assertEquals(1001L, UserContext.getUserId());
    }

    @Test
    void preHandleIgnoresMalformedLoginUserPayload() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:bad-token")).thenReturn("{bad-json");

        boolean result = authInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(result);
        assertNull(UserContext.get());
    }
}
