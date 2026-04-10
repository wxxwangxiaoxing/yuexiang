package com.yuexiang.voucher.support;

import com.yuexiang.voucher.domain.entity.PaymentRecord;
import com.yuexiang.voucher.domain.entity.RefundRecord;
import com.yuexiang.voucher.mapper.PaymentRecordMapper;
import com.yuexiang.voucher.mapper.RefundRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VoucherOrderRecordSupport {

    private final PaymentRecordMapper paymentRecordMapper;
    private final RefundRecordMapper refundRecordMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public void createPaymentRecord(
            Long userId,
            Long orderId,
            Long amount,
            Integer payType,
            String thirdPaymentNo,
            String remark
    ) {
        PaymentRecord payment = new PaymentRecord();
        payment.setPaymentNo("PAY" + snowflakeIdGenerator.nextId());
        payment.setThirdPaymentNo(thirdPaymentNo);
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setPayType(payType);
        payment.setDirection(2);
        payment.setStatus(1);
        payment.setRemark(remark);
        payment.setCreateTime(LocalDateTime.now());
        payment.setFinishTime(LocalDateTime.now());
        paymentRecordMapper.insert(payment);
    }

    public void createRefundRecord(Long userId, Long orderId, Long refundAmount, String reason) {
        RefundRecord refund = new RefundRecord();
        refund.setRefundNo("RF" + snowflakeIdGenerator.nextId());
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setRefundAmount(refundAmount);
        refund.setReason(reason);
        refund.setStatus(2);
        refund.setRefundTime(LocalDateTime.now());
        refund.setCreateTime(LocalDateTime.now());
        refundRecordMapper.insert(refund);
    }
}
