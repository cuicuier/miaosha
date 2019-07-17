package com.cui.miaosha.controller;

import com.cui.miaosha.access.AccessLimit;
import com.cui.miaosha.domain.MiaoshaOrder;
import com.cui.miaosha.domain.OrderInfo;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.rabbitmq.MQSender;
import com.cui.miaosha.rabbitmq.MiaoshaMessage;
import com.cui.miaosha.redis.AccessKey;
import com.cui.miaosha.redis.GoodsKey;
import com.cui.miaosha.redis.MiaoshaKey;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.result.CodeMsg;
import com.cui.miaosha.result.Result;
import com.cui.miaosha.service.impl.GoodsService;
import com.cui.miaosha.service.impl.MiaoshaService;
import com.cui.miaosha.service.impl.OrderService;
import com.cui.miaosha.utils.MD5Utils;
import com.cui.miaosha.utils.UUIDUtil;
import com.cui.miaosha.vo.GoodsVo;
import com.cui.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    MiaoshaService miaoshaService;
    @Autowired
    RedisService redisService;
    @Autowired
    MQSender mqSender;

    private Map<Integer, Boolean> localOverMap = new HashMap<>();
    /**
     * 系统初始化
     * 将秒杀商品库存数量加载到redis
     *
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVO();
        if (goodsVoList == null) {
            return;
        }
        for (GoodsVo goodsVo : goodsVoList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);//false表示该秒杀商品在redis中还没有失效（即还有库存）
        }
    }

    @RequestMapping("/do_miaosha")
    public String doMiaosha(User user, Model model,
                            @RequestParam("goodsId")Integer goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return "login"; //若用户未登录则先到登录页面
        }
        //判断库存
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        int stock = goodsVo.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.MIAOSHA_OVER_ERROR.getMsg());
            return "miaosha_fail";
        }
        //判断是否秒杀到(不能重复秒杀)
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());//该用户已经秒杀过该商品了，不能重复秒杀
            return "miaosha_fail";
        }
        /*
        秒杀到了，执行以下操作
        1.减库存
        2.下订单（order_info表插入相应订单记录）
        3.写入秒杀订单（miaosha_order表插入相应秒杀订单记录）
         */
        OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goodsVo);
        return "order_detail";
    }
//    @RequestMapping(value = "/do_miaosha2",method = RequestMethod.POST)
    @PostMapping("/do_miaosha2")
    @ResponseBody
    public Result<OrderInfo> doMiaosha2(User user, Model model,
                             @RequestParam("goodsId")Integer goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        if (localOverMap.get(goodsId)) {//若该商品对应内存标记为true，表示没有库存了
            return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
        }
        //预减库存
        int stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId).intValue();//jedis的decr方法返回值是Long类型
        if (stock < 0) { //stock减为0，表示库存还有一个，可以继续秒杀
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
        }

        //判断是否秒杀到(不能重复秒杀)，从redis缓存中查看是否有该用户该秒杀商品对应的秒杀订单
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        /*
        秒杀到了，执行以下操作
        1.减库存
        2.下订单（order_info表插入相应订单记录）
        3.写入秒杀订单（miaosha_order表插入相应秒杀订单记录）
         */
        //入队
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setUser(user);
        miaoshaMessage.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(miaoshaMessage);


        return Result.success(new OrderInfo());
    }

    /**
     * 客户端轮询，是否秒杀成功
     * orderId  成功
     * -1    秒杀中
     * 0    排队中
     * @param user
     * @param model
     * @param goodsId
     * @return
     */
    @GetMapping("/result")
    @ResponseBody
    public Result<Integer> miaoshaResult(User user, Model model,
                                        @RequestParam("goodsId")Integer goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        Integer orderId = miaoshaService.getMiaoshaResultByUidGid(user.getId(), goodsId);
        return Result.success(orderId);
    }

    @AccessLimit(seconds=5,maxCount=10,needLogin=true)
    @GetMapping("/path")
    @ResponseBody
    public Result<String> getMiaoshaPath(Model model, User user, HttpServletRequest request,
            @RequestParam("goodsId")Integer goodsId,
            @RequestParam(value = "verifyCode",defaultValue = "0")Integer verifyCode) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        /*//查询访问次数(这里是5秒访问5次)
        String uri = request.getRequestURI();
        String key = uri + "_" + user.getId();
        Integer count = redisService.get(AccessKey.access, key, Integer.class);
        if (count == null) { //第一次访问
            redisService.set(AccessKey.access, key, 1);
        } else if (count < 5) {
            redisService.incr(AccessKey.access, key); //访问未超过限制次数，可以再次访问，同时访问加1
        } else {//在有效时间内访问次数达到上限，不能再访问
            return Result.error(CodeMsg.ACCESS_LIMIT);
        }*/
        //验证码是否正确
        boolean checkVerifyCode=miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
        if (!checkVerifyCode){
            return Result.error(CodeMsg.REQUEST_ILLEAGAL);
        }
        String path=miaoshaService.createMiaoshaPath(user,goodsId);
        return Result.success(path);
    }

    @PostMapping("/{path}/do_miaosha")
    @ResponseBody
    public Result<OrderInfo> miaosha(Model model,User user,@RequestParam("goodsId")Integer goodsId,
                                  @PathVariable("path")String path) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证path
        boolean checkPath = miaoshaService.checkPath(user, goodsId, path);
        if (!checkPath) {//目标path不一致，请求非法
            return Result.error(CodeMsg.REQUEST_ILLEAGAL);
        }

        if (localOverMap.get(goodsId)) {//若该商品对应内存标记为true，表示没有库存了
            return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
        }
        //预减库存
        int stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId).intValue();//jedis的decr方法返回值是Long类型
        if (stock < 0) { //stock减为0，表示库存还有一个，可以继续秒杀
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAOSHA_OVER_ERROR);
        }

        //判断是否秒杀到(不能重复秒杀)，从redis缓存中查看是否有该用户该秒杀商品对应的秒杀订单
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        /*
        秒杀到了，执行以下操作
        1.减库存
        2.下订单（order_info表插入相应订单记录）
        3.写入秒杀订单（miaosha_order表插入相应秒杀订单记录）
         */
        //入队
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setUser(user);
        miaoshaMessage.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(miaoshaMessage);


        return Result.success(new OrderInfo());
    }

    @GetMapping("/verifyCode")
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(Model model, User user,
           HttpServletResponse response,
           @RequestParam("goodsId") Integer goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = miaoshaService.createMiaoshaVerifyCode(user, goodsId);
        try {
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null; //已通过outputstream返回

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

}
