package com.yuexiang.voucher.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "SessionListVO", description = "秒杀场次列表响应")
public class SessionListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "服务器当前时间戳（毫秒）")
    private Long serverTime;

    @Schema(description = "场次列表")
    private List<SessionVO> sessions;

    @Data
    @Schema(name = "SessionVO", description = "场次信息")
    public static class SessionVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "场次ID")
        private Long sessionId;

        @Schema(description = "场次名称")
        private String title;

        @Schema(description = "开始时间（毫秒时间戳）")
        private Long beginTime;

        @Schema(description = "结束时间（毫秒时间戳）")
        private Long endTime;

        @Schema(description = "状态：0=即将开始 1=抢购中 2=已结束")
        private Integer status;

        @Schema(description = "状态文案")
        private String statusDesc;

        @Schema(description = "该场次券数量")
        private Integer voucherCount;
    }
}
