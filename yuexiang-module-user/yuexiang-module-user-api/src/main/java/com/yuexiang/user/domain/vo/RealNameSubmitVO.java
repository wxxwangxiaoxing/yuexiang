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
@Schema(description = "实名认证提交结果VO")
public class RealNameSubmitVO {

    @Schema(description = "实名状态：0待审核")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;
}
