package com.cui.miaosha.controller;

import com.cui.miaosha.domain.User;
import com.cui.miaosha.rabbitmq.MQSender;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.redis.UserKey;
import com.cui.miaosha.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @Autowired
    private RedisService redisService;

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User user = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User();
        user.setId((long) 1);
        user.setNickname("cuicui");

        redisService.set(UserKey.getById,""+1, user);
        return Result.success(true);
    }

    /*测试RabbitMQ*/
    @Autowired
    private MQSender mqSender;

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
        mqSender.send("hello rabbitmq");
        return Result.success("Hi mq");
    }
    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqTopic() {
        mqSender.sendTopic("hello rabbitmq topic");
        return Result.success("Hi topic");
    }
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout() {
        mqSender.sendFanout("hello rabbitmq fanout");
        return Result.success("Hi fanout");
    }
    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> mqHeader() {
        mqSender.sendHeader("hello rabbitmq header");
        return Result.success("Hi header");
    }

}
