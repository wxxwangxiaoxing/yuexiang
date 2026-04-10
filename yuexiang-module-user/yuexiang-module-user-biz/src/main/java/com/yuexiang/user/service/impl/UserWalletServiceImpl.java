package com.yuexiang.user.service.impl;

import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.api.UserWalletService;
import com.yuexiang.user.assembler.UserWalletAssembler;
import com.yuexiang.user.domain.entity.UserWallet;
import com.yuexiang.user.domain.vo.UserWalletVO;
import com.yuexiang.user.mapper.UserWalletMapper;
import com.yuexiang.user.support.UserWalletOperationSupport;
import com.yuexiang.user.support.UserWalletSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWalletServiceImpl implements UserWalletService {

    private final UserWalletMapper userWalletMapper;
    private final UserWalletSupport userWalletSupport;
    private final UserWalletOperationSupport userWalletOperationSupport;
    private final UserWalletAssembler userWalletAssembler;

    // ==================== 公共接口方法 ====================

    @Override
    public boolean deductBalance(Long userId, Long amount) {
        userWalletOperationSupport.validateAmount(userId, amount);
        userWalletSupport.getOrCreateWallet(userId);
        return userWalletOperationSupport.executeWithRetry(userId, amount, this::doDeduct);
    }

    @Override
    public boolean deductBalance(Long userId, Integer amount) {
        Assert.notNull(amount, "amount不能为空");
        return deductBalance(userId, amount.longValue());
    }

    @Override
    public boolean refundBalance(Long userId, Long amount) {
        userWalletOperationSupport.validateAmount(userId, amount);
        return doRefund(userId, amount);
    }

    @Override
    public boolean refundBalance(Long userId, Integer amount) {
        Assert.notNull(amount, "amount不能为空");
        return refundBalance(userId, amount.longValue());
    }

    // ==================== 查询接口 ====================

    @Override
    public UserWalletVO getWalletInfo(Long userId) {
        userWalletSupport.validateUserId(userId);
        UserWallet wallet = userWalletSupport.getOrCreateWallet(userId);
        return userWalletAssembler.assemble(wallet);
    }

    @Override
    public boolean checkBalance(Long userId, Long amount) {
        userWalletSupport.validateUserId(userId);
        Assert.notNull(amount, "amount不能为空");
        UserWallet wallet = userWalletSupport.getOrCreateWallet(userId);
        return wallet.getBalance() >= amount;
    }

    @Override
    public void initWallet(Long userId) {
        userWalletSupport.validateUserId(userId);
        userWalletSupport.getOrCreateWallet(userId);
    }

    // ==================== 核心事务方法 ====================

    /**
     * 单次扣款尝试
     * 注意：这个方法内的所有逻辑都会在一个独立的事务中执行
     *
     * @return 1=成功, 0=版本冲突需重试, -1=余额不足
     */
    @Transactional(rollbackFor = Exception.class)
    protected final int doDeduct(Long userId, Long amount) {
        UserWallet wallet = userWalletMapper.selectByUserId(userId);

        // 余额预检（快速失败）
        if (wallet.getBalance() < amount) {
            return -1;
        }

        // 乐观锁扣款
        int rows = userWalletMapper.deductBalance(userId, amount, wallet.getVersion());

        if (rows > 0) {
            log.info("✅ 用户扣款成功: userId={}, amount={}, 扣款前余额={}",
                    userId, amount, wallet.getBalance());
        }
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    protected final boolean doRefund(Long userId, Long amount) {
        UserWallet wallet = userWalletMapper.selectByUserId(userId);

        int rows = userWalletMapper.refundBalance(userId, amount);
        if (rows == 0) {
            throw new BusinessException(500, "退款失败，用户钱包异常");
        }

        log.info("✅ 用户退款成功: userId={}, amount={}, 退款前余额={}",
                userId, amount, wallet.getBalance());
        return true;
    }

}
