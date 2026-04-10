package com.yuexiang.user.service.impl;

import com.yuexiang.common.exception.*;
import com.yuexiang.user.assembler.SafeUserInfo;
import com.yuexiang.user.assembler.UserMeAssembler;
import com.yuexiang.user.constant.UserAccountConstants;
import com.yuexiang.user.domain.dto.*;
import com.yuexiang.user.domain.entity.*;
import com.yuexiang.user.domain.enums.GenderEnum;
import com.yuexiang.user.domain.enums.RealNameStatusEnum;
import com.yuexiang.user.domain.vo.*;
import com.yuexiang.user.mapper.*;
import com.yuexiang.user.service.UserAccountService;
import com.yuexiang.user.support.UserAccountSupport;
import com.yuexiang.user.support.UserAuthSupport;
import com.yuexiang.user.support.UserAccountQuerySupport;
import com.yuexiang.user.support.UserPayPasswordLockSupport;
import com.yuexiang.user.support.UserProfileUpdateSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final UserWalletMapper userWalletMapper;
    private final UserAuthMapper userAuthMapper;
    private final UserAccountSupport userAccountSupport;
    private final UserMeAssembler userMeAssembler;
    private final UserAccountQuerySupport userAccountQuerySupport;
    private final UserAuthSupport userAuthSupport;
    private final UserPayPasswordLockSupport userPayPasswordLockSupport;
    private final UserProfileUpdateSupport userProfileUpdateSupport;

    // ==================== 查询接口 ====================

    @Override
    public UserMeVO getMe(Long userId) {
        User user = userAccountQuerySupport.getExistingUser(userId);
        UserInfo userInfo = userInfoMapper.selectById(userId);
        UserWallet wallet = userWalletMapper.selectById(userId);
        UserAuth auth = userAuthMapper.selectByUserId(userId);
        MemberLevel level = userAccountQuerySupport.getLevelByUserInfo(userInfo);

        return userMeAssembler.assemble(user, userInfo, wallet, auth, level);
    }

    @Override
    public UserPublicVO getPublicInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new NotFoundException("用户不存在");
        }

        UserInfo info = userInfoMapper.selectById(userId);
        MemberLevel level = userAccountQuerySupport.getLevelByUserInfo(info);
        SafeUserInfo safe = SafeUserInfo.of(info);

        return UserPublicVO.builder()
                .userId(userId)
                .nickName(user.getNickName())
                .avatar(user.getAvatar())
                .gender(safe.getGender())
                .genderDesc(GenderEnum.getDescByCode(safe.getGender()))
                .city(safe.getCity())
                .introduce(safe.getIntroduce())
                .level(safe.getLevel())
                .levelName(level != null ? level.getName() : "普通会员")
                .followCount(safe.getFollowCount())
                .fansCount(safe.getFansCount())
                .likeCount(safe.getLikeCount())
                .build();
    }

    @Override
    public RealNameVO getRealName(Long userId) {
        UserAuth auth = userAuthMapper.selectByUserId(userId);

        if (auth == null) {
            return RealNameVO.builder()
                    .status(RealNameStatusEnum.NOT_SUBMITTED.getCode())
                    .statusDesc(RealNameStatusEnum.NOT_SUBMITTED.getDesc())
                    .build();
        }

        return RealNameVO.builder()
                .status(auth.getStatus())
                .statusDesc(RealNameStatusEnum.getDescByCode(auth.getStatus()))
                .realName(userAccountSupport.maskRealName(auth.getRealName()))
                .idCard(userAccountSupport.maskIdCard(auth.getIdCard()))
                .rejectReason(auth.getRejectReason())
                .auditTime(userAccountSupport.formatDateTime(auth.getAuditTime()))
                .createTime(userAccountSupport.formatDateTime(auth.getCreateTime()))
                .build();
    }

    // ==================== 基础信息修改 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(Long userId, UpdateUserInfoDTO dto) {
        userProfileUpdateSupport.updateUserBasicInfo(userId, dto);
        userProfileUpdateSupport.updateUserExtendInfo(userId, dto);
        log.info("用户信息修改成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePhone(Long userId, UpdatePhoneDTO dto) {
        User user = userAccountQuerySupport.getExistingUser(userId);
        String oldPhone = user.getPhone();
        String newPhone = dto.getNewPhone();

        if (oldPhone.equals(newPhone)) {
            throw new BadRequestException("新手机号与当前手机号相同");
        }
        if (userMapper.selectByPhone(newPhone) != null) {
            throw new BadRequestException("新手机号已被其他账户绑定");
        }

        user.setPhone(newPhone);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户手机号修改成功: userId={}, {} -> {}",
                userId, userAccountSupport.maskPhone(oldPhone), userAccountSupport.maskPhone(newPhone));
    }

    // ==================== 登录密码管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPassword(Long userId, SetPasswordDTO dto) {
        User user = userAccountQuerySupport.getExistingUser(userId);

        if (StringUtils.hasText(user.getPassword())) {
            throw new BadRequestException("已设置密码，请使用修改密码功能");
        }

        validateAndCheckLoginPassword(dto.getPassword(), dto.getConfirmPassword());

        user.setPassword(userAccountSupport.hashPassword(dto.getPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户设置密码成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PasswordResultVO updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = userAccountQuerySupport.getExistingUser(userId);

        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("原密码错误");
        }
        if (BCrypt.checkpw(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("新密码不能与原密码相同");
        }

        validateAndCheckLoginPassword(dto.getNewPassword(), dto.getConfirmPassword());

        user.setPassword(userAccountSupport.hashPassword(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改密码成功: userId={}", userId);
        return PasswordResultVO.builder().requireReLogin(true).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        User user = userMapper.selectByPhone(dto.getPhone());
        if (user == null) {
            throw new BadRequestException("该手机号未注册");
        }

        validateAndCheckLoginPassword(dto.getNewPassword(), dto.getConfirmPassword());

        user.setPassword(userAccountSupport.hashPassword(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户重置密码成功: userId={}, phone={}", user.getId(), userAccountSupport.maskPhone(dto.getPhone()));
    }

    // ==================== 支付密码管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPayPassword(Long userId, SetPayPasswordDTO dto) {
        UserWallet wallet = userAccountQuerySupport.getExistingWallet(userId);

        if (StringUtils.hasText(wallet.getPayPassword())) {
            throw new BadRequestException("已设置支付密码，请使用修改功能");
        }

        validateAndCheckPayPassword(dto.getPayPassword(), dto.getConfirmPassword(), userId);
        doUpdatePayPassword(wallet, dto.getPayPassword());

        log.info("用户设置支付密码成功: userId={}", userId);
    }

    @Override
    public void updatePayPassword(Long userId, UpdatePayPasswordDTO dto) {
        userPayPasswordLockSupport.checkLock(userId);

        UserWallet wallet = userAccountQuerySupport.getExistingWallet(userId);
        if (!BCrypt.checkpw(dto.getOldPayPassword(), wallet.getPayPassword())) {
            userPayPasswordLockSupport.recordError(userId);
            int remaining = userPayPasswordLockSupport.getRemainingAttempts(userId);
            throw new BadRequestException("原支付密码错误，还剩" + remaining + "次机会");
        }

        validateAndCheckPayPassword(dto.getNewPayPassword(), dto.getConfirmPassword(), userId);
        userPayPasswordLockSupport.clear(userId);
        doUpdatePayPasswordInTx(wallet, dto.getNewPayPassword());

        log.info("用户修改支付密码成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPayPassword(Long userId, ResetPayPasswordDTO dto) {
        UserWallet wallet = userAccountQuerySupport.getExistingWallet(userId);

        validateAndCheckPayPassword(dto.getNewPayPassword(), dto.getConfirmPassword(), userId);
        userPayPasswordLockSupport.clear(userId);
        doUpdatePayPassword(wallet, dto.getNewPayPassword());

        log.info("用户重置支付密码成功: userId={}", userId);
    }

    // ==================== 实名认证 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RealNameSubmitVO submitRealName(Long userId, RealNameDTO dto) {
        UserAuth existAuth = userAuthMapper.selectByUserId(userId);

        if (existAuth != null && userAuthSupport.isPendingOrApproved(existAuth.getStatus())) {
            throw new BadRequestException("已提交实名认证");
        }
        if (userAuthMapper.existsByIdCard(dto.getIdCard(), userId)) {
            throw new BadRequestException("该身份证已被其他账户认证");
        }

        UserAuth auth = userAuthSupport.buildOrUpdateAuth(existAuth, userId, dto);

        if (existAuth != null) {
            userAuthMapper.updateById(auth);
        } else {
            userAuthMapper.insert(auth);
        }

        log.info("用户提交实名认证: userId={}", userId);

        return RealNameSubmitVO.builder()
                .status(RealNameStatusEnum.AUDITING.getCode())
                .statusDesc(RealNameStatusEnum.AUDITING.getDesc())
                .build();
    }

    // ==================== 账户注销 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CancelResultVO applyCancel(Long userId, CancelAccountDTO dto) {
        User user = userAccountQuerySupport.getExistingUser(userId);

        if (user.getStatus() == 3) {
            throw new BadRequestException("已在注销冷静期内");
        }

        UserWallet wallet = userWalletMapper.selectById(userId);
        if (wallet != null && wallet.getBalance() > 0) {
            throw new BadRequestException("钱包余额不为零");
        }

        user.setStatus(3);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        LocalDateTime deadline = LocalDateTime.now().plusDays(UserAccountConstants.COOLING_DAYS);
        log.info("用户申请注销: userId={}, deadline={}", userId, deadline);

        return CancelResultVO.builder()
                .cancelDeadline(deadline.format(UserAccountConstants.DT_FORMATTER))
                .coolingDays(UserAccountConstants.COOLING_DAYS)
                .tips("账户将在" + UserAccountConstants.COOLING_DAYS + "天后自动注销，期间可撤销申请")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeCancel(Long userId, RevokeCancelDTO dto) {
        User user = userAccountQuerySupport.getExistingUser(userId);

        if (user.getStatus() != 3) {
            throw new BadRequestException("无注销申请");
        }

        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户撤销注销申请: userId={}", userId);
    }

    // ==================== 支付密码更新 ====================

    private void doUpdatePayPassword(UserWallet wallet, String rawPassword) {
        wallet.setPayPassword(userAccountSupport.hashPassword(rawPassword));
        wallet.setUpdateTime(LocalDateTime.now());
        userWalletMapper.updateById(wallet);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void doUpdatePayPasswordInTx(UserWallet wallet, String rawPassword) {
        doUpdatePayPassword(wallet, rawPassword);
    }

    // ==================== 密码校验（合并） ====================

    private void validateAndCheckLoginPassword(String password, String confirmPassword) {
        validatePasswordMatch(password, confirmPassword);
        if (!UserAccountConstants.LOGIN_PWD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException("密码格式不正确，需8~20位，且包含字母和数字");
        }
    }

    private void validateAndCheckPayPassword(String payPwd, String confirmPwd, Long userId) {
        validatePasswordMatch(payPwd, confirmPwd);
        if (!UserAccountConstants.PAY_PWD_PATTERN.matcher(payPwd).matches()) {
            throw new BadRequestException("支付密码必须为6位数字");
        }
        if (payPwd.matches("(\\d)\\1{5}")) {
            throw new BadRequestException("支付密码不能为重复数字");
        }
        if (userAccountSupport.isSequentialDigits(payPwd)) {
            throw new BadRequestException("支付密码不能为连续数字");
        }
    }

    private void validatePasswordMatch(String pwd, String confirmPwd) {
        if (!pwd.equals(confirmPwd)) {
            throw new BadRequestException("两次密码不一致");
        }
    }

}
