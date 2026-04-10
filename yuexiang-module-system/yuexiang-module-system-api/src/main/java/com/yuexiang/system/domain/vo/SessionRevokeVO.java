package com.yuexiang.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话吊销响应")
public class SessionRevokeVO {

    @Schema(description = "被吊销的会话数量")
    private Integer revokedCount;
}
