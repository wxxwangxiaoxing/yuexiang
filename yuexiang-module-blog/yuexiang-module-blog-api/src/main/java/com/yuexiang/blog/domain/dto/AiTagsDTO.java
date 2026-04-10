package com.yuexiang.blog.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "AiTagsDTO", description = "AI打标签请求")
public class AiTagsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "笔记标题")
    private String title;

    @Size(min = 10, message = "正文内容至少10个字符")
    @Schema(description = "笔记正文")
    private String content;
}
