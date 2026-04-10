package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BlogLikeVO", description = "笔记点赞结果")
public class BlogLikeVO {

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "笔记点赞数")
    private Long likeCount;
}
