package com.yuexiang.voucher.task;

import com.yuexiang.voucher.service.VoucherOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final VoucherOrderService voucherOrderService;

    @Scheduled(fixedDelay = 60000)
    public void cancelTimeoutOrders() {
        try {
            int cancelled = voucherOrderService.cancelTimeoutOrders();
            if (cancelled > 0) {
                log.info("定时任务：取消超时订单，共取消 {} 笔", cancelled);
            }
        } catch (Exception e) {
            log.error("定时任务：取消超时订单异常", e);
        }
    }
}
