package com.yuexiang.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {

    int countByVoucherIdAndUserId(@Param("voucherId") Long voucherId, @Param("userId") Long userId);

    Long selectOrderIdByVoucherAndUser(@Param("voucherId") Long voucherId, @Param("userId") Long userId);
}
