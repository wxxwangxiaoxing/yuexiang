package com.yuexiang.framework.security.core;

import com.yuexiang.common.exception.UnauthorizedException;

public class UserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = get();
        if (user == null) {
            throw new UnauthorizedException();
        }
        return user.getUserId();
    }

    public static void remove() {
        USER_HOLDER.remove();
    }
}
