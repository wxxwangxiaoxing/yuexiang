package com.yuexiang.user.service;

import com.yuexiang.user.domain.dto.RepairSignDTO;
import com.yuexiang.user.domain.vo.ClaimResultVO;
import com.yuexiang.user.domain.vo.RepairResultVO;
import com.yuexiang.user.domain.vo.SignCalendarVO;
import com.yuexiang.user.domain.vo.SignRankVO;
import com.yuexiang.user.domain.vo.SignResultVO;
import com.yuexiang.user.domain.vo.SignRewardsVO;

/**
 * 签到服务接口
 *
 * 提供签到相关功能：
 * 1. 查询签到日历
 * 2. 执行签到
 * 3. 补签
 * 4. 查询签到奖励规则
 * 5. 领取签到里程碑奖励
 * 6. 查询签到排行榜
 */
public interface SignService {

    /**
     * 获取用户签到日历数据
     *
     * @param userId 用户ID
     * @param year 年份，可为空
     * @param month 月份，可为空
     * @return 签到日历数据
     */
    SignCalendarVO getCalendar(Long userId, Integer year, Integer month);

    /**
     * 用户执行签到
     *
     * @param userId 用户ID
     * @return 签到结果
     */
    SignResultVO doSign(Long userId);

    /**
     * 用户补签
     *
     * @param userId 用户ID
     * @param dto 补签请求参数
     * @return 补签结果
     */
    RepairResultVO repairSign(Long userId, RepairSignDTO dto);

    /**
     * 获取签到奖励规则
     *
     * @return 奖励规则信息
     */
    SignRewardsVO getRewards();

    /**
     * 领取里程碑奖励
     *
     * @param userId 用户ID
     * @param ruleId 奖励规则ID
     * @return 领取结果
     */
    ClaimResultVO claimReward(Long userId, Long ruleId);

    /**
     * 获取签到排行榜
     *
     * @param type 排行类型：1=本月累计签到天数，2=连续签到天数
     * @param top 返回前多少名
     * @return 排行榜数据
     */
    SignRankVO getRank(Integer type, Integer top);
}