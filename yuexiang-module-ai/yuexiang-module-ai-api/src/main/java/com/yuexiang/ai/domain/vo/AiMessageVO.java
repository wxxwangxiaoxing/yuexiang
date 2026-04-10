package com.yuexiang.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI消息VO")
public class AiMessageVO {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "角色：user/assistant/tool")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "卡片数据快照JSON")
    private String cardsData;

    @Schema(description = "工具名称")
    private String toolName;

    @Schema(description = "工具参数")
    private String toolArgs;

    @Schema(description = "工具结果")
    private String toolResult;

    @Schema(description = "完成原因")
    private String finishReason;

    @Schema(description = "本条消息Token消耗")
    private Integer tokenUsage;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "关联商户ID列表")
    private String shopIds;

    @Schema(description = "创建时间")
    private Long createTime;
}
