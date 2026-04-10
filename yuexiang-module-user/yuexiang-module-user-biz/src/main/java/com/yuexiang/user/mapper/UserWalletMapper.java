package com.yuexiang.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.user.domain.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    UserWallet selectByUserId(@Param("userId") Long userId);

    int deductBalance(@Param("userId") Long userId, @Param("amount") Long amount, @Param("version") Integer version);

    int refundBalance(@Param("userId") Long userId, @Param("amount") Long amount);

    int initWallet(@Param("userId") Long userId);
}
