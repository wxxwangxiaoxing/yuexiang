package com.yuexiang.user.service.impl;

import com.yuexiang.common.config.BusinessProperties;
import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.assembler.UserInfoVOAssembler;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.enums.PointsType;
import com.yuexiang.user.domain.vo.UserInfoVO;
import com.yuexiang.user.mapper.UserInfoMapper;
import com.yuexiang.user.api.UserPointsService;
import com.yuexiang.user.support.UserPointsAccountSupport;
import com.yuexiang.user.support.UserPointsRecordSupport;
import com.yuexiang.user.support.UserPointsValidationSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointsServiceImpl implements UserPointsService {

    private final UserInfoMapper userInfoMapper;
    private final BusinessProperties businessProperties;
    private final UserPointsValidationSupport userPointsValidationSupport;
    private final UserPointsAccountSupport userPointsAccountSupport;
    private final UserPointsRecordSupport userPointsRecordSupport;
    private final UserInfoVOAssembler userInfoVOAssembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points, String bizType,
                             Long bizId, String description) {
        userPointsValidationSupport.validateParams(userId, points);
        userPointsValidationSupport.validatePointsRange(points);

        try {
            UserInfo userInfo = userPointsAccountSupport.getOrCreateUserInfo(userId);

            int affectedRows = userInfoMapper.addPoints(userId, points);
            if (affectedRows == 0) {
                log.error("[积分增加失败] userId={}, points={}, bizType={}", userId, points, bizType);
                throw new BusinessException(ResultCodeEnum.POINTS_ADD_FAILED, 
                    String.format("积分增加失败，用户ID: %d, 积分: %d", userId, points));
            }

            UserInfo updatedUser = userInfoMapper.selectById(userId);
            if (updatedUser == null) {
                log.error("[查询用户信息失败] userId={}", userId);
                throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND, 
                    String.format("用户不存在: %d", userId));
            }
            int newBalance = updatedUser.getPoints();

            userPointsRecordSupport.savePointsRecord(userId, PointsType.EARN, points, newBalance,
                             bizType, bizId, description);

            userPointsAccountSupport.checkAndUpgradeLevel(userId, newBalance, userInfo.getLevel());

            log.info("[积分增加成功] userId={}, points={}, balance={}, bizType={}",
                     userId, points, newBalance, bizType);
            return true;
        } catch (BadRequestException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[积分增加异常] userId={}, points={}, bizType={}, error={}", 
                     userId, points, bizType, e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.POINTS_ADD_FAILED, 
                "积分增加过程中发生系统异常", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, Integer points, String bizType,
                                Long bizId, String description) {
        userPointsValidationSupport.validateParams(userId, points);
        userPointsValidationSupport.validatePointsRange(points);

        try {
            int affectedRows = userInfoMapper.deductPoints(userId, points);
            
            if (affectedRows == 0) {
                UserInfo userInfo = userInfoMapper.selectById(userId);
                if (userInfo == null) {
                    log.error("[用户不存在] userId={}", userId);
                    throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND, 
                        String.format("用户不存在: %d", userId));
                }
                log.warn("[积分不足] userId={}, 当前积分={}, 需扣减={}", 
                        userId, userInfo.getPoints(), points);
                throw new BusinessException(ResultCodeEnum.POINTS_INSUFFICIENT, 
                    String.format("积分不足: 当前积分 %d, 需扣减 %d", userInfo.getPoints(), points));
            }

            UserInfo updatedUser = userInfoMapper.selectById(userId);
            if (updatedUser == null) {
                log.error("[查询用户信息失败] userId={}", userId);
                throw new BusinessException(ResultCodeEnum.USER_NOT_FOUND, 
                    String.format("用户不存在: %d", userId));
            }
            int newBalance = updatedUser.getPoints();

            userPointsRecordSupport.savePointsRecord(userId, PointsType.DEDUCT, -points, newBalance,
                             bizType, bizId, description);

            userPointsAccountSupport.checkAndUpgradeLevel(userId, newBalance, updatedUser.getLevel());

            log.info("[积分扣减成功] userId={}, points={}, balance={}, bizType={}",
                     userId, points, newBalance, bizType);
            return true;
        } catch (BadRequestException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[积分扣减异常] userId={}, points={}, bizType={}, error={}", 
                     userId, points, bizType, e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.POINTS_DEDUCT_FAILED, 
                "积分扣减过程中发生系统异常", e);
        }
    }

    @Override
    public boolean checkAndUpgradeLevel(Long userId) {
        if (userId == null) {
            log.warn("[等级检查失败] userId为空");
            throw new BadRequestException("用户ID不能为空");
        }

        try {
            UserInfo userInfo = userInfoMapper.selectById(userId);
            if (userInfo == null) {
                log.warn("[等级检查失败] 用户不存在: userId={}", userId);
                return false;
            }
            return userPointsAccountSupport.checkAndUpgradeLevel(userId, userInfo.getPoints(), userInfo.getLevel());
        } catch (Exception e) {
            log.error("[等级检查异常] userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.LEVEL_UPGRADE_FAILED, 
                "等级检查过程中发生系统异常", e);
        }
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        if (userId == null) {
            log.warn("[查询用户信息失败] userId为空");
            throw new BadRequestException("用户ID不能为空");
        }

        try {
            UserInfo userInfo = userInfoMapper.selectById(userId);
            if (userInfo == null) {
                log.debug("[查询用户信息] 用户不存在: userId={}", userId);
                return null;
            }
            return userInfoVOAssembler.assemble(userId, userInfo);
        } catch (Exception e) {
            log.error("[查询用户信息异常] userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR,"查询用户信息过程中发生系统异常", e);
        }
    }
}
