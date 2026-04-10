package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "AiPolishVO", description = "AI润色响应")
public class AiPolishVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "原始内容")
    private String original;

    @Schema(description = "润色后内容")
    private String polished;

    @Schema(description = "修改说明")
    private List<ChangeVO> changes;

    @Schema(description = "是否为降级内容")
    private Boolean fallback;

    @Data
    @Schema(name = "ChangeVO", description = "修改说明")
    public static class ChangeVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "修改类型")
        private String type;

        @Schema(description = "修改说明")
        private String description;
    }
}
