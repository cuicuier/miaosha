package com.cui.miaosha.access;

import com.cui.miaosha.domain.User;

public class UserContext {
    /*
    线程安全
    ThreadLocal 放到当前线程，一个线程一份
     */
    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }
}
