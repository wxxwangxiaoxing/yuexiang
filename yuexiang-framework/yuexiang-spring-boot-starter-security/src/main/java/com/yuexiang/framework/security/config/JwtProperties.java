package com.yuexiang.framework.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "f8e7d6c5b4a3928173645e7d8c9b0a1f2e3d4c5b6a7980716253443526172839";

    private long accessTokenExpire = 7200;

    private long refreshTokenExpire = 604800;
}
