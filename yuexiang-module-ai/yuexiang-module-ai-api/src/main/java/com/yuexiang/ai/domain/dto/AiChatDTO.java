package com.yuexiang.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI对话请求DTO")
public class AiChatDTO {

    @Schema(description = "会话ID，首次对话不传")
    private String sessionId;

    @Schema(description = "用户问题", required = true)
    private String question;

    @Schema(description = "用户位置-经度")
    private Double lng;

    @Schema(description = "用户位置-纬度")
    private Double lat;
}
