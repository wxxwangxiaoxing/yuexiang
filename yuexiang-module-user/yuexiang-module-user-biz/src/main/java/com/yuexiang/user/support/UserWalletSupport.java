package com.yuexiang.user.support;

import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.domain.entity.UserWallet;
import com.yuexiang.user.mapper.UserWalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWalletSupport {

    private final UserWalletMapper userWalletMapper;

    public UserWallet getOrCreateWallet(Long userId) {
        UserWallet wallet = userWalletMapper.selectByUserId(userId);
        if (wallet != null) {
            return wallet;
        }
        return createWallet(userId);
    }

    public void validateUserId(Long userId) {
        Assert.notNull(userId, "userId不能为空");
    }

    private UserWallet createWallet(Long userId) {
        try {
            userWalletMapper.initWallet(userId);
            log.info("✅ 钱包初始化成功: userId={}", userId);
        } catch (DuplicateKeyException e) {
            log.debug("钱包已被并发创建: userId={}", userId);
        }

        UserWallet wallet = userWalletMapper.selectByUserId(userId);
        if (wallet == null) {
            throw new BusinessException(500, "钱包初始化失败");
        }
        return wallet;
    }
}
