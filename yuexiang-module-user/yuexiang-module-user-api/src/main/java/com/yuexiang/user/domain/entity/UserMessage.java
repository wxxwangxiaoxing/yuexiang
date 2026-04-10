package com.yuexiang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_message")
public class UserMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 消息类型：1系统通知 2点赞收藏 3新增关注 4评论和@ */
    private Integer type;

    private String title;

    private String content;

    /** 关联业务ID（笔记ID/评论ID/用户ID等） */
    private Long bizId;

    /** 是否已读：0未读 1已读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
