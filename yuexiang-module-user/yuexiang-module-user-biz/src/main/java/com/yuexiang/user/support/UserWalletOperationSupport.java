package com.yuexiang.user.support;

import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.user.constant.UserWalletConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class UserWalletOperationSupport {

    public void validateAmount(Long userId, Long amount) {
        Assert.notNull(userId, "userId不能为空");
        Assert.notNull(amount, "amount不能为空");
        if (amount <= 0) {
            throw new BusinessException(400, "金额必须大于0");
        }
        if (amount > UserWalletConstants.MAX_AMOUNT) {
            throw new BusinessException(400, "单笔金额不能超过100万元");
        }
    }

    public boolean executeWithRetry(Long userId, Long amount, WalletOperation operation) {
        for (int attempt = 1; ; attempt++) {
            int result = operation.execute(userId, amount);

            if (result > 0) {
                return true;
            }
            if (result < 0) {
                throw new BusinessException(400, "余额不足");
            }
            if (attempt >= UserWalletConstants.MAX_RETRY) {
                throw new BusinessException(500, "系统繁忙，请稍后重试");
            }

            sleep(attempt * 50L);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "操作被中断");
        }
    }

    @FunctionalInterface
    public interface WalletOperation {
        int execute(Long userId, Long amount);
    }
}
