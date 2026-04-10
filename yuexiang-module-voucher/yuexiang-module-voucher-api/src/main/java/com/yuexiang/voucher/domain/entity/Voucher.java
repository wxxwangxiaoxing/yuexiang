package com.yuexiang.voucher.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_voucher")
public class Voucher {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    private String title;

    private String subTitle;

    private String rules;

    private Integer payValue;

    private Integer actualValue;

    private Integer type;

    private Integer status;

    private LocalDateTime validBeginTime;

    private LocalDateTime validEndTime;

    private Long createdBy;

    private Long updatedBy;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
