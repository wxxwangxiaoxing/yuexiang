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
@Schema(description = "个人中心首页VO")
public class UserProfileVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "展示用ID（带前缀格式化）")
    private String userIdDisplay;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "头像路径")
    private String avatar;

    @Schema(description = "性别：0未知 1男 2女")
    private Integer gender;

    @Schema(description = "会员等级（1~10）")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "等级图标")
    private String levelIcon;

    @Schema(description = "当前积分")
    private Integer points;

    @Schema(description = "关注数")
    private Integer followCount;

    @Schema(description = "粉丝数")
    private Integer fansCount;

    @Schema(description = "获赞总数（笔记被点赞数）")
    private Integer likeCount;

    @Schema(description = "今日是否已签到")
    private Boolean isSignedToday;

    @Schema(description = "连续签到天数")
    private Integer continuousSignDays;

    @Schema(description = "未读消息数")
    private Integer unreadMessageCount;

    @Schema(description = "待支付订单数")
    private Integer unpaidOrderCount;

    @Schema(description = "未使用优惠券数")
    private Integer unusedVoucherCount;
}
