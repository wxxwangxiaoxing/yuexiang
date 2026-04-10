package com.yuexiang.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_blog_likes")
@Schema(name = "BlogLike", description = "笔记点赞记录实体")
public class BlogLike implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联笔记ID")
    private Long blogId;

    @Schema(description = "点赞用户ID")
    private Long userId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
