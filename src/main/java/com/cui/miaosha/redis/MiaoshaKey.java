package com.cui.miaosha.redis;

public class MiaoshaKey extends BasePrefix {
    public MiaoshaKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix);
    }

    public MiaoshaKey(String prefix) {
        super(prefix);
    }

    public static MiaoshaKey isGoodsOver = new MiaoshaKey("goodsover"); //表示redis中的库存没了
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey("miaoshaPath");
    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300,"miaoshaVerifyCode");
}
