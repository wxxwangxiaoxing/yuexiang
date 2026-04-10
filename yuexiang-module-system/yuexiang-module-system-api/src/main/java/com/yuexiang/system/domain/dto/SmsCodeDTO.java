package com.yuexiang.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发送短信验证码请求")
public class SmsCodeDTO {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号(含国际区号，如 +8613812345678)", example = "+8613800138000")
    private String phone;

    @NotNull(message = "验证码类型不能为空")
    @Schema(description = "用途：0登录 1注册 2找回密码 3绑定手机 4验旧手机 5设登录密码 6设支付密码 7注销账户", example = "0")
    private Integer type;

    @NotBlank(message = "图形验证码ID不能为空")
    @Schema(description = "图形验证码ID")
    private String captchaId;

    @NotBlank(message = "图形验证码不能为空")
    @Schema(description = "图形验证码结果")
    private String captchaCode;
}
