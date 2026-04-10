package com.yuexiang.framework.security.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.UnauthorizedException;
import com.yuexiang.framework.security.config.JwtProperties;
import com.yuexiang.framework.security.constant.JwtRedisConstants;
import com.yuexiang.framework.security.core.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JWT 令牌提供者 - 支持令牌滚动刷新(Rotation)与安全重用检测
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtTokenProvider(JwtProperties jwtProperties, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.verifier = JWT.require(algorithm).build();
    }

    public TokenPair generateTokenPair(Long userId) {
        String familyId = generateUuid();
        return createTokenPair(userId, familyId);
    }

    private TokenPair createTokenPair(Long userId, String familyId) {
        String accessJti = generateUuid();
        String refreshJti = generateUuid();
        long now = System.currentTimeMillis() / 1000;

        String accessToken = JWT.create()
                .withClaim("typ", "access")
                .withClaim("jti", accessJti)
                .withClaim("uid", userId)
                .withClaim("iat", now)
                .withClaim("exp", now + jwtProperties.getAccessTokenExpire())
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withClaim("typ", "refresh")
                .withClaim("jti", refreshJti)
                .withClaim("uid", userId)
                .withClaim("familyId", familyId)
                .withClaim("iat", now)
                .withClaim("exp", now + jwtProperties.getRefreshTokenExpire())
                .sign(algorithm);

        String refreshKey = String.format(JwtRedisConstants.AUTH_REFRESH_KEY, familyId);
        redisTemplate.opsForValue().set(
                refreshKey,
                refreshJti,
                jwtProperties.getRefreshTokenExpire(),
                TimeUnit.SECONDS
        );

        String sessionKey = String.format(JwtRedisConstants.AUTH_USER_SESSIONS_KEY, userId);
        redisTemplate.opsForSet().add(sessionKey, accessJti, refreshJti);

        LoginUser loginUser = new LoginUser(userId, null);
        try {
            String userJson = objectMapper.writeValueAsString(loginUser);
            String tokenKey = "token:" + accessToken;
            redisTemplate.opsForValue().set(tokenKey, userJson, jwtProperties.getAccessTokenExpire(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("存储用户信息到Redis失败: {}", e.getMessage());
        }

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn((int) jwtProperties.getAccessTokenExpire())
                .absoluteExpiresIn((int) jwtProperties.getRefreshTokenExpire())
                .accessJti(accessJti)
                .refreshJti(refreshJti)
                .familyId(familyId)
                .build();
    }

    public TokenPair refreshToken(String refreshToken) {
        DecodedJWT jwt = parseToken(refreshToken);
        String typ = jwt.getClaim("typ").asString();

        if (!"refresh".equals(typ)) {
            throw new BadRequestException("无效的刷新令牌类型");
        }

        String jti = jwt.getClaim("jti").asString();
        String familyId = jwt.getClaim("familyId").asString();
        Long userId = jwt.getClaim("uid").asLong();

        if (userId == null) {
            throw new UnauthorizedException("令牌信息不完整");
        }

        String refreshKey = String.format(JwtRedisConstants.AUTH_REFRESH_KEY, familyId);
        String currentValidJti = redisTemplate.opsForValue().get(refreshKey);

        if (currentValidJti == null) {
            throw new UnauthorizedException("会话已过期，请重新登录");
        }

        if (!jti.equals(currentValidJti)) {
            revokeTokenFamily(userId, familyId);
            throw new UnauthorizedException("检测到异常令牌重用，安全起见当前账号已强制下线");
        }

        return createTokenPair(userId, familyId);
    }

    public DecodedJWT parseToken(String token) {
        try {
            return verifier.verify(token);
        } catch (TokenExpiredException e) {
            throw new UnauthorizedException("令牌已过期");
        } catch (JWTVerificationException e) {
            throw new UnauthorizedException("无效的令牌");
        }
    }

    public boolean validateAccessToken(String token) {
        try {
            DecodedJWT jwt = parseToken(token);
            String jti = jwt.getClaim("jti").asString();
            String blacklistKey = String.format(JwtRedisConstants.AUTH_BLACKLIST_KEY, jti);
            return !Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            return false;
        }
    }

    public void addToBlacklist(String token) {
        DecodedJWT jwt = parseToken(token);
        String jti = jwt.getClaim("jti").asString();
        Long exp = jwt.getClaim("exp").asLong();
        Long userId = jwt.getClaim("uid").asLong();

        long now = System.currentTimeMillis() / 1000;
        long ttl = exp - now;

        if (ttl > 0) {
            String blacklistKey = String.format(JwtRedisConstants.AUTH_BLACKLIST_KEY, jti);
            redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.SECONDS);
        }

        String sessionKey = String.format(JwtRedisConstants.AUTH_USER_SESSIONS_KEY, userId);
        redisTemplate.opsForSet().remove(sessionKey, jti);
    }

    public int revokeAllSessions(Long userId) {
        String sessionKey = String.format(JwtRedisConstants.AUTH_USER_SESSIONS_KEY, userId);
        Set<String> jtis = redisTemplate.opsForSet().members(sessionKey);

        if (jtis == null || jtis.isEmpty()) {
            return 0;
        }

        for (String jti : jtis) {
            String blacklistKey = String.format(JwtRedisConstants.AUTH_BLACKLIST_KEY, jti);
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    "1",
                    jwtProperties.getRefreshTokenExpire(),
                    TimeUnit.SECONDS
            );
        }

        redisTemplate.delete(sessionKey);
        return jtis.size();
    }

    private void revokeTokenFamily(Long userId, String familyId) {
        String refreshKey = String.format(JwtRedisConstants.AUTH_REFRESH_KEY, familyId);
        redisTemplate.delete(refreshKey);
        revokeAllSessions(userId);
        log.warn("检测到令牌重用风险！已吊销用户 {} (Family: {}) 的所有会话", userId, familyId);
    }

    private String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Long getUserId(String token) {
        return parseToken(token).getClaim("uid").asLong();
    }

    public String getJti(String token) {
        return parseToken(token).getClaim("jti").asString();
    }
}
