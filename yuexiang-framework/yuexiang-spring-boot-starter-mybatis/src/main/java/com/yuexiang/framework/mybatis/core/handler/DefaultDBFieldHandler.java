package com.yuexiang.framework.mybatis.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = resolveCurrentUserId();

        if (metaObject.hasSetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", () -> now, LocalDateTime.class);
        }
        if (metaObject.hasSetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", () -> now, LocalDateTime.class);
        }
        if (metaObject.hasSetter("createdBy") && currentUserId != null) {
            this.strictInsertFill(metaObject, "createdBy", () -> currentUserId, Long.class);
        }
        if (metaObject.hasSetter("updatedBy") && currentUserId != null) {
            this.strictInsertFill(metaObject, "updatedBy", () -> currentUserId, Long.class);
        }
        if (metaObject.hasSetter("deleted")) {
            this.strictInsertFill(metaObject, "deleted", () -> 0, Integer.class);
        }
        if (metaObject.hasSetter("version")) {
            this.strictInsertFill(metaObject, "version", () -> 0, Integer.class);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = resolveCurrentUserId();

        this.strictUpdateFill(metaObject, "updateTime", () -> now, LocalDateTime.class);
        if (metaObject.hasSetter("updatedBy") && currentUserId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", () -> currentUserId, Long.class);
        }
    }

    private Long resolveCurrentUserId() {
        try {
            Class<?> userContextClass = Class.forName("com.yuexiang.framework.security.core.UserContext");
            Object userId = userContextClass.getMethod("getUserId").invoke(null);
            return userId instanceof Long ? (Long) userId : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
