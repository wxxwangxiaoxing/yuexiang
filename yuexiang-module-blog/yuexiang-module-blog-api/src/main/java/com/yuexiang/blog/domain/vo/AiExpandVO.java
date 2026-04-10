package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "AiExpandVO", description = "AI扩写响应")
public class AiExpandVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "原始内容")
    private String original;

    @Schema(description = "扩写后内容")
    private String expanded;

    @Schema(description = "字数")
    private Integer wordCount;

    @Schema(description = "是否为降级内容")
    private Boolean fallback;
}
