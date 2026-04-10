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
@Schema(description = "实名认证信息VO")
public class RealNameVO {

    @Schema(description = "实名状态：-1未提交 0待审核 1已通过 2已拒绝")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "真实姓名(脱敏)")
    private String realName;

    @Schema(description = "身份证号(脱敏)")
    private String idCard;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "提交时间")
    private String createTime;
}
