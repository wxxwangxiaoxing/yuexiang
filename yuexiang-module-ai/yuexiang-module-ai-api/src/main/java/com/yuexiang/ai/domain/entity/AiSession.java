package com.yuexiang.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_ai_session")
public class AiSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private Long userId;

    private String title;

    private Integer messageCount;

    private Integer status;

    private String shopIds;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastActiveTime;

    private Integer totalTokens;

    private String summary;

    private String contextJson;

    private BigDecimal longitude;

    private BigDecimal latitude;
}
