package com.yuexiang.user.assembler;

import com.yuexiang.user.domain.entity.MemberLevel;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.vo.UserProfileVO;
import org.springframework.stereotype.Component;

@Component
public class UserProfileAssembler {

    public UserProfileVO assemble(
            Long userId,
            User user,
            UserInfo userInfo,
            MemberLevel level,
            boolean signedToday,
            int continuousDays
    ) {
        return UserProfileVO.builder()
                .userId(userId)
                .userIdDisplay("HM_" + userId)
                .nickName(user.getNickName())
                .avatar(user.getAvatar())
                .gender(userInfo.getGender())
                .level(userInfo.getLevel())
                .levelName(level != null ? level.getName() : "普通会员")
                .levelIcon(level != null ? level.getIcon() : null)
                .points(userInfo.getPoints())
                .followCount(userInfo.getFollowCount())
                .fansCount(userInfo.getFansCount())
                .likeCount(userInfo.getLikeCount())
                .isSignedToday(signedToday)
                .continuousSignDays(continuousDays)
                .unreadMessageCount(0)
                .unpaidOrderCount(0)
                .unusedVoucherCount(0)
                .build();
    }
}
