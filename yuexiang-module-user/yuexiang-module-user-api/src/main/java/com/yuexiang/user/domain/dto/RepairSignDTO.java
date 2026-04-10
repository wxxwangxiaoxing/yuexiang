package com.yuexiang.user.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairSignDTO {

    @NotNull(message = "年份不能为空")
    @Min(value = 2020, message = "年份不合法")
    @Max(value = 2100, message = "年份不合法")
    private Integer year;

    @NotNull(message = "月份不能为空")
    @Min(value = 1, message = "月份不合法")
    @Max(value = 12, message = "月份不合法")
    private Integer month;

    @NotNull(message = "日期不能为空")
    @Min(value = 1, message = "日期不合法")
    @Max(value = 31, message = "日期不合法")
    private Integer day;
}
