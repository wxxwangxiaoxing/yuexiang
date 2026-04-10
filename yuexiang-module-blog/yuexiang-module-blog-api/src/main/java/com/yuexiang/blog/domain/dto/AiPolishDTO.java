package com.yuexiang.blog.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "AiPolishDTO", description = "AI润色请求")
public class AiPolishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(min = 10, max = 10000, message = "正文长度需在10-10000字符之间")
    @Schema(description = "原始正文", required = true)
    private String content;

    @Schema(description = "风格偏好(lively/literary/professional/humorous)")
    private String style = "lively";
}
