package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(name = "BlogDetailVO", description = "笔记详情响应")
public class BlogDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "笔记ID")
    private Long id;

    @Schema(description = "笔记标题")
    private String title;

    @Schema(description = "笔记正文")
    private String content;

    @Schema(description = "笔记图片列表")
    private List<String> images;

    @Schema(description = "作者信息")
    private UserVO user;

    @Schema(description = "关联商户信息")
    private ShopVO shop;

    @Schema(description = "标签列表")
    private List<TagVO> tags;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "收藏数")
    private Integer favoriteCount;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否已收藏")
    private Boolean isFavorited;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Data
    @Schema(name = "UserVO", description = "用户简要信息")
    public static class UserVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "昵称")
        private String nickName;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "是否已关注")
        private Boolean isFollowed;
    }

    @Data
    @Schema(name = "ShopVO", description = "商户简要信息")
    public static class ShopVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "商户ID")
        private Long id;

        @Schema(description = "商户名称")
        private String name;

        @Schema(description = "地址")
        private String address;

        @Schema(description = "人均价格(分)")
        private Integer avgPrice;
    }

    @Data
    @Schema(name = "TagVO", description = "标签信息")
    public static class TagVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "标签ID")
        private Long id;

        @Schema(description = "标签名称")
        private String name;
    }
}
