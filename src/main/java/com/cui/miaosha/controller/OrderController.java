package com.cui.miaosha.controller;

import com.cui.miaosha.domain.OrderInfo;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.result.CodeMsg;
import com.cui.miaosha.result.Result;
import com.cui.miaosha.service.impl.GoodsService;
import com.cui.miaosha.service.impl.OrderService;
import com.cui.miaosha.vo.GoodsVo;
import com.cui.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private GoodsService goodsService;
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> orderDetail(@RequestParam("orderId") Integer orderId, User user) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        Integer goodsId = orderInfo.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrderInfo(orderInfo);
        orderDetailVo.setGoodsVo(goodsVo);
        return Result.success(orderDetailVo);
    }

}
