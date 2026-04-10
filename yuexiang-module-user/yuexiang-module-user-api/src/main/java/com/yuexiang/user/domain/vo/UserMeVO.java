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
@Schema(description = "当前用户完整信息VO")
public class UserMeVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号(脱敏)")
    private String phone;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "性别描述")
    private String genderDesc;

    @Schema(description = "生日")
    private String birthday;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "个人介绍")
    private String introduce;

    @Schema(description = "会员等级(1~10)")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "当前积分")
    private Integer points;

    @Schema(description = "关注数")
    private Integer followCount;

    @Schema(description = "粉丝数")
    private Integer fansCount;

    @Schema(description = "获赞总数")
    private Integer likeCount;

    @Schema(description = "钱包余额(分)")
    private Long balance;

    @Schema(description = "是否已设置登录密码")
    private Boolean hasPassword;

    @Schema(description = "是否已设置支付密码")
    private Boolean hasPayPassword;

    @Schema(description = "实名状态：-1未提交 0待审核 1已通过 2已拒绝")
    private Integer realNameStatus;

    @Schema(description = "实名状态描述")
    private String realNameStatusDesc;

    @Schema(description = "注册时间")
    private String createTime;
}
