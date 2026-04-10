package com.yuexiang.user.api;

import com.yuexiang.user.domain.vo.SignCalendarVO;
import com.yuexiang.user.domain.vo.SignResultVO;
import com.yuexiang.user.domain.vo.UserProfileVO;

public interface UserProfileService {

    UserProfileVO getProfile(Long userId);

    SignCalendarVO getSignCalendar(Long userId, Integer year, Integer month);

    SignResultVO sign(Long userId);
}
