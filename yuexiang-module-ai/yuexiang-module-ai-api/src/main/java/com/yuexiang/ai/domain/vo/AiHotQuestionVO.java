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
@Schema(description = "AI热门问题VO")
public class AiHotQuestionVO {

    @Schema(description = "问题ID")
    private Integer id;

    @Schema(description = "问题内容")
    private String text;

    @Schema(description = "图标")
    private String icon;
}
