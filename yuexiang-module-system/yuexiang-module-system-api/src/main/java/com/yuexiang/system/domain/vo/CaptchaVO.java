package com.yuexiang.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图形验证码响应")
public class CaptchaVO {

    @Schema(description = "验证码ID，发送短信时需透传")
    private String captchaId;

    @Schema(description = "Base64编码的PNG图片数据")
    private String image;

    @Schema(description = "有效期秒数")
    private Integer expireSeconds;
}
