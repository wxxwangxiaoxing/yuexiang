package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "AiTitleVO", description = "AI生成标题响应")
public class AiTitleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "候选标题列表")
    private List<String> titles;

    @Schema(description = "是否为降级内容")
    private Boolean fallback;
}
