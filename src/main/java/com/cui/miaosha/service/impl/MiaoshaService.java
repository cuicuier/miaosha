package com.cui.miaosha.service.impl;

import com.cui.miaosha.dao.GoodsDao;
import com.cui.miaosha.domain.Goods;
import com.cui.miaosha.domain.MiaoshaOrder;
import com.cui.miaosha.domain.OrderInfo;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.redis.MiaoshaKey;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.utils.MD5Utils;
import com.cui.miaosha.utils.UUIDUtil;
import com.cui.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService {
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    RedisService redisService;

    /*
    秒杀到了，执行以下操作(原子操作)
    1.减库存
    2.下订单（order_info表插入相应订单记录）
    3.写入秒杀订单（miaosha_order表插入相应秒杀订单记录）
     */
    @Transactional
    public OrderInfo miaosha(User user, GoodsVo goodsVo) {
        //减库存
        boolean success=goodsService.reduceStock(goodsVo);
        //下订单(写入两张表，order_info,miaosha_order)
        if (success) {
            return orderService.createOrder(user, goodsVo);
        } else {
            //减库存失败
            setGoodsOver(goodsVo.getId());
            return null;
        }
    }



    public Integer getMiaoshaResultByUidGid(Long userId, Integer goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {
            return order.getOrderId();
        } else {
            boolean isGoodsOver = getGoodsOver(goodsId);
            if (isGoodsOver) {
                return -1; //没有库存了
            } else {
                return 0; //继续轮询
            }

        }
    }
    private void setGoodsOver(Integer goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(Integer goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver,""+goodsId); //redis中存在这个key，表示该商品已卖完了
    }

    public boolean checkPath(User user, Integer goodsId, String path) {
        String redisPath = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);
        if (path.equals(redisPath)) {
            return true;
        } else {
            return false;
        }
    }

    public String createMiaoshaPath(User user,Integer goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        String path = MD5Utils.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, path);
        return path;
    }

    public BufferedImage createMiaoshaVerifyCode(User user, Integer goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码计算结果存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+"_"+goodsId, rnd);
        //输出图片
        return image;

    }

    private static int calc(String verifyCode) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(verifyCode);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * + - *
     * @param rdm
     * @return
     */
    private static char[] ops={'+','-','*'};
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+num1 + op1 + num2 + op2 + num3;
        return exp;

    }

    public boolean checkVerifyCode(User user, Integer goodsId, Integer verifyCode) {
        if (user == null || goodsId <= 0) {
            return false;
        }
        Integer redisVerifyCode = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId() + "_" + goodsId, Integer.class);
        if (redisVerifyCode == null || redisVerifyCode - verifyCode != 0) {
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId() + "_" + goodsId);
        return true;

    }
}
