package com.yuexiang.voucher.support;

import com.yuexiang.voucher.domain.entity.Voucher;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import com.yuexiang.voucher.domain.vo.VoucherDetailVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;
import com.yuexiang.voucher.mapper.VoucherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoucherConverter {

    private final VoucherMapper voucherMapper;

    public VoucherDetailVO toDetailVO(Voucher voucher) {
        return VoucherDetailVO.builder()
                .id(voucher.getId())
                .shopId(voucher.getShopId())
                .title(voucher.getTitle())
                .subTitle(voucher.getSubTitle())
                .rules(voucher.getRules())
                .payValue(voucher.getPayValue() / 100.0)
                .actualValue(voucher.getActualValue() / 100.0)
                .type(voucher.getType())
                .status(voucher.getStatus())
                .validBeginTime(voucher.getValidBeginTime())
                .validEndTime(voucher.getValidEndTime())
                .build();
    }

    public VoucherOrderVO toOrderVO(VoucherOrder order) {
        Voucher voucher = voucherMapper.selectById(order.getVoucherId());
        return VoucherOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .voucherId(order.getVoucherId())
                .voucherTitle(voucher != null ? voucher.getTitle() : null)
                .payValue(voucher != null ? voucher.getPayValue() / 100.0 : null)
                .actualValue(voucher != null ? voucher.getActualValue() / 100.0 : null)
                .status(order.getStatus())
                .payType(order.getPayType())
                .createTime(order.getCreateTime())
                .payTime(order.getPayTime())
                .useTime(order.getUseTime())
                .build();
    }
}
