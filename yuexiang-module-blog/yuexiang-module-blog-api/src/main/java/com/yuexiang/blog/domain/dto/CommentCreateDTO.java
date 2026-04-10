package com.yuexiang.blog.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "CommentCreateDTO", description = "发表评论请求")
public class CommentCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 2048, message = "评论内容长度需在1-2048字符之间")
    @Schema(description = "评论内容", required = true)
    private String content;

    @Schema(description = "顶级评论ID(0=发表顶级评论)")
    private Long rootCommentId = 0L;

    @Schema(description = "回复目标评论ID(0=非楼中楼回复)")
    private Long replyCommentId = 0L;
}
