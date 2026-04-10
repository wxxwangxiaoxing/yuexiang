package com.yuexiang.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignRankVO {

    private Integer rankType;
    private String rankTypeDesc;
    private Long updateTime;
    private MyRankVO myRank;
    private List<RankItemVO> list;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyRankVO {
        private Integer rank;
        private Integer signDays;
        private Long userId;
        private String nickName;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankItemVO {
        private Integer rank;
        private Integer signDays;
        private Long userId;
        private String nickName;
        private String avatar;
        private String levelName;
    }
}
