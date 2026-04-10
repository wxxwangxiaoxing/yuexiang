package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "CommentCreateVO", description = "发表评论响应")
public class CommentCreateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "创建时间")
    private String createTime;
}
