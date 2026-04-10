package com.yuexiang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI会话详情VO")
public class AiSessionDetailVO {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @Schema(description = "推荐商户列表")
    private List<RecommendedShopVO> shops;

    @Schema(description = "消息列表")
    private List<AiMessageVO> messages;

    @Schema(description = "创建时间")
    private Long createTime;
}
