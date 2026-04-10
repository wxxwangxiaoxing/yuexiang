package com.yuexiang.framework.security.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {

    private String accessToken;

    private String refreshToken;

    private Integer expiresIn;

    private Integer absoluteExpiresIn;

    private String accessJti;

    private String refreshJti;

    private String familyId;
}
