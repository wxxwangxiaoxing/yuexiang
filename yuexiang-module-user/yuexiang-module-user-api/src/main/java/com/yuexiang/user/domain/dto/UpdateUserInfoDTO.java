package com.yuexiang.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "修改用户基本信息请求")
public class UpdateUserInfoDTO {

    @Size(min = 2, max = 32, message = "昵称长度需在2~32字符之间")
    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "头像路径")
    private String avatar;

    @Schema(description = "性别：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Size(max = 32, message = "城市最多32字符")
    @Schema(description = "城市")
    private String city;

    @Size(max = 256, message = "个人介绍最多256字符")
    @Schema(description = "个人介绍签名")
    private String introduce;
}
