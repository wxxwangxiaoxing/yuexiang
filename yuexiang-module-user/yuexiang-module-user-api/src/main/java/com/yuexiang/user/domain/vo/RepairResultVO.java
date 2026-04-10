package com.yuexiang.user.domain.vo;

import com.yuexiang.user.domain.vo.SignResultVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairResultVO {

    private String signDate;
    private Integer costPoints;
    private Integer remainPoints;
    private Integer continuousDays;
    private Boolean continuousChanged;
    private SignResultVO.MilestoneVO unlockedMilestone;
}
