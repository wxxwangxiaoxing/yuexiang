package com.yuexiang.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_user_wallet")
public class UserWallet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long balance;

    private Long frozenBalance;

    private Long totalRecharge;

    private Long totalConsume;

    private String payPassword;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
