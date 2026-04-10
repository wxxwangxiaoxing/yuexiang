package com.yuexiang.voucher.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.voucher.service.VoucherOrderService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "支付回调接口")
@RestController
@RequestMapping("/api/pay/callback")
@RequiredArgsConstructor
public class PayCallbackController {

    private final VoucherOrderService voucherOrderService;

    @Hidden
    @PostMapping("/wechat")
    public String wechatCallback(@RequestBody String xmlData) {
        log.info("收到微信支付回调");
        try {
            String transactionId = parseTransactionId(xmlData);
            String outTradeNo = parseOutTradeNo(xmlData);
            Long orderId = Long.parseLong(outTradeNo);
            boolean success = voucherOrderService.handlePayCallback(transactionId, orderId, 2);
            return success ? "success" : "fail";
        } catch (Exception e) {
            log.error("微信支付回调处理异常", e);
            return "fail";
        }
    }

    @Hidden
    @PostMapping("/alipay")
    public String alipayCallback(@RequestParam(value = "trade_status") String trade_status,
                                  @RequestParam(value = "trade_no") String trade_no,
                                  @RequestParam(value = "out_trade_no") String out_trade_no) {
        log.info("收到支付宝回调 trade_status={}", trade_status);
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
            try {
                Long orderId = Long.parseLong(out_trade_no);
                voucherOrderService.handlePayCallback(trade_no, orderId, 3);
                return "success";
            } catch (Exception e) {
                log.error("支付宝回调处理异常", e);
                return "fail";
            }
        }
        return "fail";
    }

    @Operation(summary = "手动触发支付回调（测试用）")
    @PostMapping("/test")
    public CommonResult<Boolean> testCallback(
            @Parameter(description = "第三方流水号") @RequestParam(value = "thirdPaymentNo") String thirdPaymentNo,
            @Parameter(description = "订单ID") @RequestParam(value = "orderId") Long orderId,
            @Parameter(description = "支付方式：2微信，3支付宝") @RequestParam(value = "payType") Integer payType) {
        return CommonResult.success(voucherOrderService.handlePayCallback(thirdPaymentNo, orderId, payType));
    }

    private String parseTransactionId(String xmlData) {
        int start = xmlData.indexOf("<transaction_id>") + "<transaction_id>".length();
        int end = xmlData.indexOf("</transaction_id>");
        return xmlData.substring(start, end);
    }

    private String parseOutTradeNo(String xmlData) {
        int start = xmlData.indexOf("<out_trade_no>") + "<out_trade_no>".length();
        int end = xmlData.indexOf("</out_trade_no>");
        return xmlData.substring(start, end);
    }
}
