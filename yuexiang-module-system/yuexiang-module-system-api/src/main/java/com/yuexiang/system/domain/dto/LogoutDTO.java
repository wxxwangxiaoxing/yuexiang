package com.yuexiang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登出请求")
public class LogoutDTO {

    @Schema(description = "刷新令牌（可选，如果提供则同时作废）")
    private String refreshToken;
}
