package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "设置登录密码请求")
public class SetPasswordDTO {

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度需在8~20位之间")
    @Schema(description = "新密码")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码")
    private String confirmPassword;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "手机验证码(type=5)")
    private String code;
}
