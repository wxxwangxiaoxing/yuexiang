package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改支付密码请求")
public class UpdatePayPasswordDTO {

    @NotBlank(message = "原支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    @Schema(description = "原支付密码")
    private String oldPayPassword;

    @NotBlank(message = "新支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    @Schema(description = "新支付密码")
    private String newPayPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认新密码")
    private String confirmPassword;
}
