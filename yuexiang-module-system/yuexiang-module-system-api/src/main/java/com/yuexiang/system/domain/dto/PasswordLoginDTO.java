package com.yuexiang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "密码登录请求")
public class PasswordLoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "+8613800138000")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "Abc12345")
    private String password;
}
