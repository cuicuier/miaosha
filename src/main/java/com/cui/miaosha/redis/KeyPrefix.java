package com.cui.miaosha.redis;

public interface KeyPrefix {
    int expireSeconds();

    String getPrefix();

}
