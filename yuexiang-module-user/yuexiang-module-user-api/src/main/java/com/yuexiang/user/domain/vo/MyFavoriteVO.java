package com.yuexiang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的收藏VO")
public class MyFavoriteVO {

    @Schema(description = "收藏记录ID")
    private Long favoriteId;

    @Schema(description = "业务类型：1商户 2笔记")
    private Integer bizType;

    @Schema(description = "业务类型文案")
    private String bizTypeDesc;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "图片")
    private String image;

    @Schema(description = "副标题")
    private String subTitle;

    @Schema(description = "评分（商户）")
    private BigDecimal score;

    @Schema(description = "作者ID（笔记）")
    private Long authorId;

    @Schema(description = "作者名称（笔记）")
    private String authorName;

    @Schema(description = "作者头像（笔记）")
    private String authorAvatar;

    @Schema(description = "收藏时间")
    private Long createTime;
}
