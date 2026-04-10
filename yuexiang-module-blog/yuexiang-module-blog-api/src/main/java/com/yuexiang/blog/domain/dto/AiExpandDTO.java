package com.yuexiang.blog.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "AiExpandDTO", description = "AI扩写请求")
public class AiExpandDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

//    @Size(min = 5, max = 10000, message = "内容长度需在5-10000字符之间")
    @Schema(description = "原始内容", required = true)
    private String content;

    @Schema(description = "目标篇幅(short/medium/long)")
    private String targetLength = "medium";
}
