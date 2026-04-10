package com.yuexiang.voucher.service;

import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.voucher.domain.dto.VoucherCreateDTO;
import com.yuexiang.voucher.domain.vo.VoucherDetailVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;

public interface ShopVoucherService {

    Long createVoucher(Long shopId, VoucherCreateDTO dto);

    boolean updateVoucher(Long shopId, Long voucherId, VoucherCreateDTO dto);

    boolean deleteVoucher(Long shopId, Long voucherId);

    PageResult<VoucherDetailVO> getShopVoucherList(Long shopId, Integer type, Integer status, Integer pageNo, Integer pageSize);

    boolean onlineVoucher(Long shopId, Long voucherId);

    boolean offlineVoucher(Long shopId, Long voucherId);

    PageResult<VoucherOrderVO> getShopOrderList(Long shopId, Integer status, Integer pageNo, Integer pageSize);

    boolean verifyOrder(Long shopId, String orderNo);
}
