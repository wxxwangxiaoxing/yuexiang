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
@Schema(description = "我的笔记VO")
public class MyBlogVO {

    @Schema(description = "笔记ID")
    private Long blogId;

    @Schema(description = "笔记标题")
    private String title;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "关联商户ID")
    private Long shopId;

    @Schema(description = "商户名称")
    private String shopName;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "状态：0待审核 1已发布 2已屏蔽 3草稿")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusDesc;

    @Schema(description = "创建时间")
    private Long createTime;
}
