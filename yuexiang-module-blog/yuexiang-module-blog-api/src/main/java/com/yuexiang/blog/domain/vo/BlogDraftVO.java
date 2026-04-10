package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(name = "BlogDraftVO", description = "草稿保存响应")
public class BlogDraftVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "笔记ID")
    private Long blogId;

    @Schema(description = "保存时间")
    private LocalDateTime savedTime;
}
