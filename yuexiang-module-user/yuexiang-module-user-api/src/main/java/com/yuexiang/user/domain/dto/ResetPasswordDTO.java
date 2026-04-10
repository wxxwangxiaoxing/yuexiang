package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "重置登录密码请求")
public class ResetPasswordDTO {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码(type=2)")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度需在8~20位之间")
    @Schema(description = "新密码")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码")
    private String confirmPassword;
}
