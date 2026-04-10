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

    private String messageType;

    private String shopIds;

    private String cardsData;

    private String toolName;

    private String toolArgs;

    private String toolResult;

    private String finishReason;

    private Integer tokenUsage;

    private String errorCode;

    private LocalDateTime createTime;
}
