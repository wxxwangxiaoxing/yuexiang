package com.yuexiang.shop.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {

    private Long id;

    private Long userId;

    private String nickName;

    private String avatar;

    private Integer score;

    private Integer scoreTaste;

    private Integer scoreEnv;

    private Integer scoreService;

    private String content;

    private List<String> images;

    private Integer likeCount;

    private Boolean isLiked;

    private LocalDateTime createTime;
}
