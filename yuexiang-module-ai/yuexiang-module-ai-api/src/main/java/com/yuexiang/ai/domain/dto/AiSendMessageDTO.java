package com.yuexiang.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI发送消息请求DTO")
public class AiSendMessageDTO {

    @Schema(description = "会话ID，首次发送可不传")
    private String sessionId;

    @Schema(description = "用户问题", required = true)
    private String question;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;
}
