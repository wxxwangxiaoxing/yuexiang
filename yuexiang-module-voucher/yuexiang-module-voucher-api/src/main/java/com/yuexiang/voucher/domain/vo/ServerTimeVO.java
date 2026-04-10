package com.yuexiang.voucher.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "ServerTimeVO", description = "服务器时间响应")
public class ServerTimeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "服务器当前时间戳（毫秒）")
    private Long serverTime;
}
