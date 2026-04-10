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
@Schema(description = "浏览足迹项VO")
public class BrowseHistoryItemVO {

    @Schema(description = "足迹记录ID")
    private Long historyId;

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

    @Schema(description = "最近浏览时间")
    private Long viewTime;

    @Schema(description = "累计浏览次数")
    private Integer viewCount;
}
