package com.yuexiang.user.assembler;

import com.yuexiang.user.domain.entity.MemberLevel;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserAuth;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.entity.UserWallet;
import com.yuexiang.user.domain.enums.GenderEnum;
import com.yuexiang.user.domain.enums.RealNameStatusEnum;
import com.yuexiang.user.domain.vo.UserMeVO;
import com.yuexiang.user.support.UserAccountSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class UserMeAssembler {

    private final UserAccountSupport userAccountSupport;

    public UserMeVO assemble(
            User user,
            UserInfo userInfo,
            UserWallet wallet,
            UserAuth auth,
            MemberLevel level
    ) {
        SafeUserInfo safe = SafeUserInfo.of(userInfo);

        return UserMeVO.builder()
                .userId(user.getId())
                .phone(userAccountSupport.maskPhone(user.getPhone()))
                .nickName(user.getNickName())
                .avatar(user.getAvatar())
                .gender(safe.getGender())
                .genderDesc(GenderEnum.getDescByCode(safe.getGender()))
                .birthday(userInfo != null && userInfo.getBirthday() != null
                        ? userInfo.getBirthday().toString() : null)
                .city(safe.getCity())
                .introduce(safe.getIntroduce())
                .level(safe.getLevel())
                .levelName(level != null ? level.getName() : "普通会员")
                .points(safe.getPoints())
                .followCount(safe.getFollowCount())
                .fansCount(safe.getFansCount())
                .likeCount(safe.getLikeCount())
                .balance(wallet != null ? wallet.getBalance() : 0L)
                .hasPassword(StringUtils.hasText(user.getPassword()))
                .hasPayPassword(wallet != null && StringUtils.hasText(wallet.getPayPassword()))
                .realNameStatus(auth != null ? auth.getStatus() : -1)
                .realNameStatusDesc(RealNameStatusEnum.getDescByCode(
                        auth != null ? auth.getStatus() : -1))
                .createTime(userAccountSupport.formatDateTime(user.getCreateTime()))
                .build();
    }
}
