package com.yuexiang.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "浏览足迹分页VO")
public class BrowseHistoryPageVO {

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "按日期分组的记录")
    private List<BrowseHistoryGroupVO> groups;
}
