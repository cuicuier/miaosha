package com.cui.miaosha.redis;

public class UserKey extends BasePrefix {
    private static final int TOKEN_EXPIRE=3600*24*2;//有效期两天
     private UserKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix); //0表示永不过期
    }


    public static UserKey getById = new UserKey(0,"id");
    public static UserKey token = new UserKey(TOKEN_EXPIRE,"tk");
    public static UserKey getByName = new UserKey(0,"name ");
}
