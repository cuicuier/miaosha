package com.cui.miaosha.rabbitmq;

import com.cui.miaosha.domain.MiaoshaOrder;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.service.impl.GoodsService;
import com.cui.miaosha.service.impl.MiaoshaService;
import com.cui.miaosha.service.impl.OrderService;
import com.cui.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message) {
        logger.info("receive message:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        logger.info("receive topic queue1 message:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        logger.info("receive topic queue2 message:"+message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE1)
    public void receiveHeaderQueue(byte[] meaasge) {
        logger.info("receive header queue1 message:"+new String(meaasge));
    }

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receiveMiaoshaQueue(String message) {
        logger.info("receive message:"+message);
        MiaoshaMessage miaoshaMessage = RedisService.StringToBean(message, MiaoshaMessage.class);
        User user = miaoshaMessage.getUser();
        Integer goodsId = miaoshaMessage.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        int stock = goodsVo.getStockCount();
        if (stock <= 0) {
            return;
        }
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {//已经秒杀过，不能重复秒杀
            return;
        }
        //下单，写入秒杀订单
        miaoshaService.miaosha(user, goodsVo);
    }
}
