package org.xioamila.common.context;

import org.xioamila.entity.User;

public class UserContext {
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setUser(User user) {
        USER_HOLDER.set(user);
    }

    /**
     * 获取当前用户
     */
    public static User getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 清除用户信息
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}