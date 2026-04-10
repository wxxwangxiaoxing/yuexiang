package com.yuexiang.user.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer type;

    private String typeDesc;

    private String title;

    private String content;

    private Long bizId;

    private Integer isRead;

    private Long createTime;
}
