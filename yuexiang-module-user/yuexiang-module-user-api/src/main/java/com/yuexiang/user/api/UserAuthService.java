package com.yuexiang.user.api;

import com.yuexiang.user.domain.entity.User;

public interface UserAuthService {

    User findByPhone(String phone);

    User registerUser(String phone, String nickName, String avatar);
}
