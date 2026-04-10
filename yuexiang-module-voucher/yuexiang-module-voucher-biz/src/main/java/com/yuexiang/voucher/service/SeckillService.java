package com.yuexiang.voucher.service;

import com.yuexiang.voucher.domain.dto.SeckillOrderDTO;
import com.yuexiang.voucher.domain.vo.*;

import java.time.LocalDate;

public interface SeckillService {

    ServerTimeVO getServerTime();

    SessionListVO getSessions(LocalDate date);

    SeckillVoucherListVO getVouchers(Long sessionId, Integer page, Integer pageSize);

    SeckillVoucherDetailVO getVoucherDetail(Long voucherId, Long userId);

    SeckillOrderVO doSeckill(SeckillOrderDTO dto, Long userId);

    SeckillOrderResultVO getOrderResult(Long orderId, Long userId);
}
