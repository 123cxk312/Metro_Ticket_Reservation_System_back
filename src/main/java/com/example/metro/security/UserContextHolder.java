package com.example.metro.security;

public final class UserContextHolder {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(CurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static CurrentUser getRequired() {
        CurrentUser currentUser = CURRENT_USER.get();

        if (currentUser == null) {
            throw new IllegalStateException("当前请求没有登录用户");
        }

        return currentUser;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
