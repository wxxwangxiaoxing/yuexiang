package com.yuexiang.user.api;

import com.yuexiang.user.domain.vo.UserWalletVO;

public interface UserWalletService {

    // 主方法，所有新代码都应该调用这个
    boolean deductBalance(Long userId, Long amount);

    // 保留Integer作为兼容重载
    boolean deductBalance(Long userId, Integer amount);

    boolean refundBalance(Long userId, Long amount);

    boolean refundBalance(Long userId, Integer amount);

    UserWalletVO getWalletInfo(Long userId);

    boolean checkBalance(Long userId, Long amount);

    void initWallet(Long userId);
}