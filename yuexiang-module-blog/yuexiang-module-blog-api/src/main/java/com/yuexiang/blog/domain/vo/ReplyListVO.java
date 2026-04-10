package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(name = "ReplyListVO", description = "子评论列表响应")
public class ReplyListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "子评论列表")
    private List<ReplyVO> list;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "总页数")
    private Integer pages;

    @Data
    @Schema(name = "ReplyVO", description = "回复信息")
    public static class ReplyVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "评论ID")
        private Long id;

        @Schema(description = "评论用户")
        private UserVO user;

        @Schema(description = "回复目标用户")
        private ReplyToUserVO replyTo;

        @Schema(description = "评论内容")
        private String content;

        @Schema(description = "点赞数")
        private Integer likeCount;

        @Schema(description = "是否已点赞")
        private Boolean isLiked;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;
    }

    @Data
    @Schema(name = "UserVO", description = "用户简要信息")
    public static class UserVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "昵称")
        private String nickName;

        @Schema(description = "头像")
        private String avatar;
    }

    @Data
    @Schema(name = "ReplyToUserVO", description = "回复目标用户信息")
    public static class ReplyToUserVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "昵称")
        private String nickName;
    }
}
