package com.yuexiang.voucher.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "SeckillOrderDTO", description = "秒杀下单请求")
public class SeckillOrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "优惠券ID不能为空")
    @Min(value = 1, message = "优惠券ID必须大于0")
    @Schema(description = "秒杀优惠券ID", required = true)
    private Long voucherId;
}
