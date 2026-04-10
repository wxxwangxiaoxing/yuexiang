package com.yuexiang.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_like_dead_letter")
public class LikeDeadLetter implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String messageId;

    private Long authorId;

    private Long blogId;

    private Long likeUserId;

    private Integer delta;

    private String errorMsg;

    private Integer retryCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
