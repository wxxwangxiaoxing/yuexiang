package com.yuexiang.user.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.user.domain.dto.RepairSignDTO;
import com.yuexiang.user.domain.vo.ClaimResultVO;
import com.yuexiang.user.domain.vo.RepairResultVO;
import com.yuexiang.user.domain.vo.SignCalendarVO;
import com.yuexiang.user.domain.vo.SignRankVO;
import com.yuexiang.user.domain.vo.SignResultVO;
import com.yuexiang.user.domain.vo.SignRewardsVO;
import com.yuexiang.user.service.SignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 签到日历相关接口
 * <p>
 * 提供功能：
 * 1. 获取签到日历
 * 2. 执行签到
 * 3. 补签
 * 4. 获取签到奖励规则
 * 5. 领取里程碑奖励
 * 6. 获取签到排行榜
 */
@Validated
@Tag(name = "签到日历")
@RestController
@RequestMapping("/api/sign")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    /**
     * 获取签到日历数据
     *
     * @param year 年份，可为空，默认当前年
     * @param month 月份，可为空，默认当前月
     * @return 签到日历数据
     */
    @Operation(summary = "获取签到日历数据")
    @GetMapping("/calendar")
    public CommonResult<SignCalendarVO> getCalendar(
            @Parameter(description = "年份")
            @RequestParam(value = "year", required = false) Integer year,

            @Parameter(description = "月份")
            @RequestParam(value = "month", required = false) Integer month) {

        return CommonResult.success(signService.getCalendar(getCurrentUserId(), year, month));
    }

    /**
     * 执行签到
     *
     * @return 签到结果
     */
    @Operation(summary = "执行签到")
    @PostMapping
    public CommonResult<SignResultVO> doSign() {
        return CommonResult.success(signService.doSign(getCurrentUserId()));
    }

    /**
     * 补签
     *
     * @param dto 补签请求参数
     * @return 补签结果
     */
    @Operation(summary = "补签")
    @PostMapping("/repair")
    public CommonResult<RepairResultVO> repairSign(@Valid @RequestBody RepairSignDTO dto) {
        return CommonResult.success(signService.repairSign(getCurrentUserId(), dto));
    }

    /**
     * 获取签到奖励规则
     *
     * @return 奖励规则信息
     */
    @Operation(summary = "获取签到奖励规则")
    @GetMapping("/rewards")
    public CommonResult<SignRewardsVO> getRewards() {
        return CommonResult.success(signService.getRewards());
    }

    /**
     * 领取里程碑奖励
     *
     * @param ruleId 奖励规则ID
     * @return 领取结果
     */
    @Operation(summary = "领取里程碑奖励")
    @PostMapping("/rewards/{ruleId}/claim")
    public CommonResult<ClaimResultVO> claimReward(
            @PathVariable
            @Parameter(description = "奖励规则ID")
            Long ruleId) {

        return CommonResult.success(signService.claimReward(getCurrentUserId(), ruleId));
    }

    /**
     * 获取签到排行榜
     *
     * @param type 排行类型：1=本月累计天数，2=连续签到天数
     * @param top 排名前 N
     * @return 排行榜数据
     */
    @Operation(summary = "获取签到排行榜")
    @GetMapping("/rank")
    public CommonResult<SignRankVO> getRank(
            @Parameter(description = "排行类型: 1=本月累计天数, 2=连续签到天数")
            @RequestParam(value = "type", required = false, defaultValue = "1")
            @Min(value = 1, message = "排行类型最小为1")
            @Max(value = 2, message = "排行类型最大为2")
            Integer type,

            @Parameter(description = "排名数量")
            @RequestParam(value = "top", required = false, defaultValue = "20")
            @Min(value = 1, message = "排名数量不能小于1")
            @Max(value = 100, message = "排名数量不能大于100")
            Integer top) {

        return CommonResult.success(signService.getRank(type, top));
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }
}