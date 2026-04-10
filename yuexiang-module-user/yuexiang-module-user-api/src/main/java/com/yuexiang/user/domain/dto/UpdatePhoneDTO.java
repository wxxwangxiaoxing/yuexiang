package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "修改手机号请求")
public class UpdatePhoneDTO {

    @NotBlank(message = "原手机号验证码不能为空")
    @Schema(description = "原手机号验证码(发送时type=4)")
    private String oldCode;

    @NotBlank(message = "新手机号不能为空")
    @Schema(description = "新手机号")
    private String newPhone;

    @NotBlank(message = "新手机号验证码不能为空")
    @Schema(description = "新手机号验证码(发送时type=3)")
    private String newCode;
}
