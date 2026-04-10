package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "CommentLikeVO", description = "评论点赞响应")
public class CommentLikeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "点赞数")
    private Integer likeCount;
}
