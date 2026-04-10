package com.yuexiang.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_sms_code")
public class SmsCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String code;

    private Integer type;

    private Integer status;

    private String ip;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}
