package com.yuexiang.voucher.service;

import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.voucher.domain.vo.PaymentRecordVO;
import com.yuexiang.voucher.domain.vo.RefundRecordVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;

public interface VoucherOrderService {

    VoucherOrderVO getOrderDetail(Long orderId, Long userId);

    boolean payOrder(Long orderId, Long userId, Integer payType);

    boolean cancelOrder(Long orderId, Long userId);

    boolean useRefund(Long orderId, Long userId, String reason);

    boolean handlePayCallback(String thirdPaymentNo, Long orderId, Integer payType);

    int cancelTimeoutOrders();

    PageResult<VoucherOrderVO> getMyOrders(Long userId, Integer status, Integer pageNo, Integer pageSize);

    PageResult<PaymentRecordVO> getPaymentRecords(Long userId, Integer pageNo, Integer pageSize);

    PageResult<RefundRecordVO> getRefundRecords(Long userId, Integer status, Integer pageNo, Integer pageSize);
}
