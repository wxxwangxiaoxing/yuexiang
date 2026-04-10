package com.yuexiang.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token刷新响应")
public class TokenRefreshVO {

    @Schema(description = "新的访问令牌")
    private String accessToken;

    @Schema(description = "新的刷新令牌")
    private String refreshToken;

    @Schema(description = "accessToken剩余秒数")
    private Integer expiresIn;
}
