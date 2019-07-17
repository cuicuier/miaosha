package com.cui.miaosha.redis;

public class OrderKey extends BasePrefix {
    private static final int TOKEN_EXPIRE=3600*24*2;//有效期两天
     private OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds,prefix); //0表示永不过期
    }


    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey(0,"miaoshaOrder");
}
