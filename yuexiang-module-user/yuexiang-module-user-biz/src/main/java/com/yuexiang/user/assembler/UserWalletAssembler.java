package com.yuexiang.user.assembler;

import com.yuexiang.user.domain.entity.UserWallet;
import com.yuexiang.user.domain.vo.UserWalletVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserWalletAssembler {

    public UserWalletVO assemble(UserWallet wallet) {
        return UserWalletVO.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .frozenBalance(wallet.getFrozenBalance())
                .totalRecharge(wallet.getTotalRecharge())
                .totalConsume(wallet.getTotalConsume())
                .hasPayPassword(StringUtils.hasText(wallet.getPayPassword()))
                .build();
    }
}
