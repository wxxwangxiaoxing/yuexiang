package com.yuexiang.voucher.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillListVO {

    private LocalDateTime serverTime;

    private List<SeckillVoucherVO> records;
}
