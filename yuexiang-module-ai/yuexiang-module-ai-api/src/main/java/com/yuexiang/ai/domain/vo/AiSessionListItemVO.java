package com.yuexiang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI会话列表项VO")
public class AiSessionListItemVO {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @Schema(description = "会话状态")
    private Integer status;

    @Schema(description = "累计Token数")
    private Integer totalTokens;

    @Schema(description = "最近活跃时间")
    private Long lastActiveTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
