package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "TagSuggestVO", description = "标签推荐响应")
public class TagSuggestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "热度")
    private Integer hot;
}
