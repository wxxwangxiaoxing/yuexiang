package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "AiSummaryVO", description = "AI摘要响应")
public class AiSummaryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "摘要文本")
    private String summary;

    @Schema(description = "是否来自缓存")
    private Boolean cached;

    @Schema(description = "是否为降级内容")
    private Boolean fallback;
}
