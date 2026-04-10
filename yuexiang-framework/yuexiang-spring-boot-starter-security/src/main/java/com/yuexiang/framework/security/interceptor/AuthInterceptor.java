package com.yuexiang.framework.security.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            return true;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            String userJson = stringRedisTemplate.opsForValue().get("token:" + token);
            if (userJson != null) {
                LoginUser loginUser = objectMapper.readValue(userJson, LoginUser.class);
                UserContext.set(loginUser);
            }
        } catch (Exception e) {
            log.warn("解析用户信息失败: {}", e.getMessage());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }
}
