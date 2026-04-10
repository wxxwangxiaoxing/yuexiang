package com.yuexiang.shop.domain.vo;

import lombok.Data;

@Data
public class ReviewSummaryVO {

    private Double avgScore;

    private Double avgScoreTaste;

    private Double avgScoreEnv;

    private Double avgScoreService;

    private Integer totalReviews;

    private Integer score5Count;

    private Integer score4Count;

    private Integer score3Count;

    private Integer score2Count;

    private Integer score1Count;
}
