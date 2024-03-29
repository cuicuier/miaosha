package com.cui.miaosha.rabbitmq;

import com.cui.miaosha.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
    private static Logger logger = LoggerFactory.getLogger(MQSender.class);

    @Autowired
    AmqpTemplate amqpTemplate;
    public void send(Object messasge) {
        String msg = RedisService.beanToString(messasge);
        logger.info("send message:"+msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);

    }

    public void sendTopic(Object message) {
        String msg = RedisService.beanToString(message);
        logger.info("send topic meaasge:" + msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY1,msg+"1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY2,msg+"2");
    }
    public void sendFanout(Object message) {
        String msg = RedisService.beanToString(message);
        logger.info("send fanout meaasge:" + msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"", msg+"1");
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg+"2");
    }

    public void sendHeader(Object message) {
        String msg = RedisService.beanToString(message);
        logger.info("send header meaasge:" + msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1", "value1");
        properties.setHeader("header2", "value2");
        Message obj = new Message(msg.getBytes(), properties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",obj);
    }

    public void sendMiaoshaMessage(MiaoshaMessage miaoshaMessage) {
        String msg = RedisService.beanToString(miaoshaMessage);
        logger.info("send miaoshaMessage: " + msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }
}
