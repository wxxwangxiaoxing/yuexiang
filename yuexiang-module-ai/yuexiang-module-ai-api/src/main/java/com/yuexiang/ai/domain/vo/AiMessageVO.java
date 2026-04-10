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

    @Schema(description = "角色：user/assistant")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "关联商户ID列表")
    private String shopIds;

    @Schema(description = "创建时间")
    private Long createTime;
}
