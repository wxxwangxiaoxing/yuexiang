package com.yuexiang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "密码修改结果VO")
public class PasswordResultVO {

    @Schema(description = "是否需要重新登录")
    private Boolean requireReLogin;
}
