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
@Schema(description = "注销申请结果VO")
public class CancelResultVO {

    @Schema(description = "注销截止时间")
    private String cancelDeadline;

    @Schema(description = "冷静期天数")
    private Integer coolingDays;

    @Schema(description = "提示信息")
    private String tips;
}
