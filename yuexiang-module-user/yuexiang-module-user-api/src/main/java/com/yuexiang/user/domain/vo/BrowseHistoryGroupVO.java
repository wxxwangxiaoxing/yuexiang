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
@Schema(description = "浏览足迹分组VO")
public class BrowseHistoryGroupVO {

    @Schema(description = "日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "日期描述（今天/昨天/03-23）")
    private String dateDesc;

    @Schema(description = "该日期的浏览记录列表")
    private List<BrowseHistoryItemVO> list;
}
