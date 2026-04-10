package com.yuexiang.user.service;

import com.yuexiang.user.domain.dto.*;
import com.yuexiang.user.domain.vo.*;

public interface UserAccountService {

    UserMeVO getMe(Long userId);

    UserPublicVO getPublicInfo(Long userId);

    void updateInfo(Long userId, UpdateUserInfoDTO dto);

    void updatePhone(Long userId, UpdatePhoneDTO dto);

    void setPassword(Long userId, SetPasswordDTO dto);

    PasswordResultVO updatePassword(Long userId, UpdatePasswordDTO dto);

    void resetPassword(ResetPasswordDTO dto);

    void setPayPassword(Long userId, SetPayPasswordDTO dto);

    void updatePayPassword(Long userId, UpdatePayPasswordDTO dto);

    void resetPayPassword(Long userId, ResetPayPasswordDTO dto);

    RealNameSubmitVO submitRealName(Long userId, RealNameDTO dto);

    RealNameVO getRealName(Long userId);

    CancelResultVO applyCancel(Long userId, CancelAccountDTO dto);

    void revokeCancel(Long userId, RevokeCancelDTO dto);
}
