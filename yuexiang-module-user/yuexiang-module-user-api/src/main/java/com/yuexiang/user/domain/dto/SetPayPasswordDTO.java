package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "设置支付密码请求")
public class SetPayPasswordDTO {

    @NotBlank(message = "支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    @Schema(description = "6位数字支付密码")
    private String payPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认支付密码")
    private String confirmPassword;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "手机验证码(type=6)")
    private String code;
}
