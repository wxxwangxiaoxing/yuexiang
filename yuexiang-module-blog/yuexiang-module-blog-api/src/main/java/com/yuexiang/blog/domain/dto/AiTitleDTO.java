package com.yuexiang.blog.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "AiTitleDTO", description = "AI生成标题请求")
public class AiTitleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(min = 10, message = "正文内容至少10个字符")
    @Schema(description = "正文内容", required = true)
    private String content;

    @Schema(description = "图片路径列表")
    private List<String> images;
}
