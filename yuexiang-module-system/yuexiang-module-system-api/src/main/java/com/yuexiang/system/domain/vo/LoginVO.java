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
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "访问令牌(JWT)，有效期2小时")
    private String accessToken;
    @Schema(description = "刷新令牌，有效期7天")
    private String refreshToken;

    @Schema(description = "accessToken剩余秒数")
    private Integer expiresIn;

    @Schema(description = "refreshToken总有效期秒数")
    private Integer absoluteExpiresIn;

    @Schema(description = "用户基本信息")
    private UserSimpleVO user;
}
