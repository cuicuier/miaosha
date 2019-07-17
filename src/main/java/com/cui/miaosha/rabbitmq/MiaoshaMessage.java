package com.cui.miaosha.rabbitmq;

import com.cui.miaosha.domain.User;

public class MiaoshaMessage {
    private User user;
    private Integer goodsId;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }
}
