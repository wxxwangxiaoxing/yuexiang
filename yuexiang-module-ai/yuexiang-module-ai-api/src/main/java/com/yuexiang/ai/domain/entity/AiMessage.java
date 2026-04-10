package com.yuexiang.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_ai_message")
public class AiMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private String role;

    private String content;

    private String shopIds;

    private LocalDateTime createTime;
}
