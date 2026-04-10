package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "申请注销账户请求")
public class CancelAccountDTO {

    @Size(max = 255, message = "注销原因最多255字符")
    @Schema(description = "注销原因")
    private String reason;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "手机验证码(type=7)")
    private String code;
}
