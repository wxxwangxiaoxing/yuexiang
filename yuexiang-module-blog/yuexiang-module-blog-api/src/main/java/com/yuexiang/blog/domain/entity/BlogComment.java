package com.yuexiang.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_blog_comments")
@Schema(name = "BlogComment", description = "笔记评论实体")
public class BlogComment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "评论用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "关联笔记ID")
    @TableField("blog_id")
    private Long blogId;

    @Schema(description = "顶级评论ID(0=自身为顶级)")
    @TableField("root_comment_id")
    private Long rootCommentId;

    @Schema(description = "回复目标评论ID(0=非回复)")
    @TableField("reply_comment_id")
    private Long replyCommentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "点赞数")
    @TableField("like_count")
    private Integer likeCount;

    @Schema(description = "状态(0正常,1隐藏)")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
