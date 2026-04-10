package com.yuexiang.user.assembler;

import com.yuexiang.user.domain.entity.MemberLevel;
import com.yuexiang.user.domain.entity.User;
import com.yuexiang.user.domain.entity.UserAuth;
import com.yuexiang.user.domain.entity.UserInfo;
import com.yuexiang.user.domain.entity.UserWallet;
import com.yuexiang.user.domain.vo.UserMeVO;
import com.yuexiang.user.support.UserAccountSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMeAssemblerTest {

    private final UserMeAssembler userMeAssembler = new UserMeAssembler(new UserAccountSupport());

    @Test
    void assembleBuildsFullUserMeView() {
        User user = new User();
        user.setId(1001L);
        user.setPhone("13800138000");
        user.setPassword("hashed-password");
        user.setNickName("测试用户");
        user.setAvatar("avatar.png");
        user.setCreateTime(LocalDateTime.of(2026, 4, 4, 12, 30, 15));

        UserInfo userInfo = new UserInfo();
        userInfo.setGender(1);
        userInfo.setBirthday(LocalDate.of(1998, 8, 8));
        userInfo.setCity("上海");
        userInfo.setIntroduce("简介");
        userInfo.setLevel(3);
        userInfo.setPoints(120);
        userInfo.setFollowCount(9);
        userInfo.setFansCount(8);
        userInfo.setLikeCount(7);

        UserWallet wallet = new UserWallet();
        wallet.setBalance(6600L);
        wallet.setPayPassword("123456");

        UserAuth auth = new UserAuth();
        auth.setStatus(1);

        MemberLevel level = new MemberLevel();
        level.setName("白银会员");

        UserMeVO result = userMeAssembler.assemble(user, userInfo, wallet, auth, level);

        assertEquals(1001L, result.getUserId());
        assertEquals("138****8000", result.getPhone());
        assertEquals("男", result.getGenderDesc());
        assertEquals("1998-08-08", result.getBirthday());
        assertEquals("白银会员", result.getLevelName());
        assertEquals(6600L, result.getBalance());
        assertTrue(result.getHasPassword());
        assertTrue(result.getHasPayPassword());
        assertEquals("已认证", result.getRealNameStatusDesc());
        assertEquals("2026-04-04T12:30:15", result.getCreateTime());
    }

    @Test
    void assembleFallsBackToSafeDefaultsWhenOptionalDataMissing() {
        User user = new User();
        user.setId(1002L);
        user.setPhone("13800138001");
        user.setNickName("默认用户");

        UserMeVO result = userMeAssembler.assemble(user, null, null, null, null);

        assertEquals(1, result.getLevel());
        assertEquals("普通会员", result.getLevelName());
        assertEquals(0, result.getPoints());
        assertEquals(0L, result.getBalance());
        assertFalse(result.getHasPassword());
        assertFalse(result.getHasPayPassword());
        assertEquals("未提交", result.getRealNameStatusDesc());
    }
}
