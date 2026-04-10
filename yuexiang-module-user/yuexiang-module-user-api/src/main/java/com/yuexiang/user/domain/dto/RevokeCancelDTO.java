package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "撤销注销申请请求")
public class RevokeCancelDTO {

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "手机验证码(type=7)")
    private String code;
}
