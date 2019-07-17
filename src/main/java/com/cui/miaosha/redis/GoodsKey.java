package com.cui.miaosha.redis;

public class GoodsKey extends BasePrefix{


    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60, "goodslist");
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "goodsdetail");
    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0, "goodsStock");

}
