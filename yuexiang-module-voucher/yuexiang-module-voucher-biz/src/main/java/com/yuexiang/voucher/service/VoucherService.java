package com.yuexiang.voucher.service;

import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.voucher.domain.vo.VoucherDetailVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;

import java.util.List;

public interface VoucherService {

    VoucherDetailVO getVoucherDetail(Long voucherId);

    PageResult<VoucherDetailVO> getShopVouchers(Long shopId, Integer pageNo, Integer pageSize);

    PageResult<VoucherOrderVO> getMyVouchers(Long userId, Integer status, Integer pageNo, Integer pageSize);

    PageResult<VoucherOrderVO> getAvailableVouchers(Long userId, Long shopId, Integer pageNo, Integer pageSize);

    List<VoucherOrderVO> getExpiringSoonVouchers(Long userId, int days);
}
