package com.yuexiang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI探店记录VO")
public class AiRecordVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "对话标题（首条用户问题）")
    private String title;

    @Schema(description = "AI回复预览（前100字）")
    private String preview;

    @Schema(description = "推荐商户数量")
    private Integer shopCount;

    @Schema(description = "创建时间")
    private Long createTime;
}
