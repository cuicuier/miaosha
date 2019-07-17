package com.cui.miaosha.service.impl;

import com.cui.miaosha.dao.OrderDao;
import com.cui.miaosha.domain.MiaoshaOrder;
import com.cui.miaosha.domain.OrderInfo;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.redis.OrderKey;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private RedisService redisService;

    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(Long userId,Integer goodsId) {
//        return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        //通过查缓存（redis中存储秒杀订单），减少查数据库
        return redisService.get(OrderKey.getMiaoshaOrderByUidGid, "" + userId + "_" + goodsId, MiaoshaOrder.class);
    }


    @Transactional
    public OrderInfo createOrder(User user, GoodsVo goodsVo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setOrderStatus(0); //0 未支付
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo); //mybatis会自动将插入成功的对象生成的id赋值给对象
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goodsVo.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);
        redisService.set(OrderKey.getMiaoshaOrderByUidGid, "" + miaoshaOrder.getUserId() + "_" + miaoshaOrder.getGoodsId(), miaoshaOrder);
        return orderInfo;
    }

    public OrderInfo getOrderById(Integer orderId) {
        return orderDao.getOrderById(orderId);
    }
}
