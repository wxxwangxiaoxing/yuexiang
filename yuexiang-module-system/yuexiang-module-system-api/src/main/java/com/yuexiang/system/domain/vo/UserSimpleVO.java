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
@Schema(description = "用户简要信息")
public class UserSimpleVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "头像")
    private String avatar;
}
