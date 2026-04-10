package com.yuexiang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户钱包VO")
public class UserWalletVO {

    @Schema(description = "钱包ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "可用余额（分）")
    private Long balance;

    @Schema(description = "冻结余额（分）")
    private Long frozenBalance;

    @Schema(description = "累计充值（分）")
    private Long totalRecharge;

    @Schema(description = "累计消费（分）")
    private Long totalConsume;

    @Schema(description = "是否设置支付密码")
    private Boolean hasPayPassword;
}
