package com.cui.miaosha.controller;

import com.cui.miaosha.domain.User;
import com.cui.miaosha.redis.GoodsKey;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.result.Result;
import com.cui.miaosha.service.impl.GoodsService;
import com.cui.miaosha.service.UserService;
import com.cui.miaosha.vo.GoodsDetailVo;
import com.cui.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;


    /**
     * 跳转到商品列表页面
     * @return
     */
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {


        model.addAttribute("user", user); //user参数自动赋值,从request中获取cookie，再获取已登录的user对象
        //查询商品列表
        List<GoodsVo> goodsVoList = goodsService.listGoodsVO();
        model.addAttribute("goodsList", goodsVoList);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        //手动渲染
        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }

    /**
     * 获取商品详情页
     */
    @RequestMapping(value = "/to_detail/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail(Model model, User user, @PathVariable("goodsId")Integer goodsId,
                         HttpServletRequest request,HttpServletResponse response) {

        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);
        //
        long startAt=goodsVo.getStartDate().getTime();
        long endAt=goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;//秒杀状态
        int remainSeconds = 0;//距离秒杀开始时间（秒）
        if (now < startAt) { //秒杀还没开始
            miaoshaStatus = 0;
            remainSeconds= (int) ((startAt-now)/1000);
        } else if (now > endAt) {//秒杀 已经结束
            miaoshaStatus = 2;
            remainSeconds=-1;
        } else {
            miaoshaStatus=1; //秒杀正在进行
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail, goodsId.toString(), String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        //手动渲染
        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, goodsId.toString(), html);
        }
        return html;

    }
    /**
     * 获取商品详情页
     * 页面静态化，ajax动态加载数据
     */
    @RequestMapping(value = "/to_detail2/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail2(Model model, User user, @PathVariable("goodsId")Integer goodsId,
                                         HttpServletRequest request, HttpServletResponse response) {

        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.getGoodsByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);
        //
        long startAt=goodsVo.getStartDate().getTime();
        long endAt=goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;//秒杀状态
        int remainSeconds = 0;//距离秒杀开始时间（秒）
        if (now < startAt) { //秒杀还没开始
            miaoshaStatus = 0;
            remainSeconds= (int) ((startAt-now)/1000);
        } else if (now > endAt) {//秒杀 已经结束
            miaoshaStatus = 2;
            remainSeconds=-1;
        } else {
            miaoshaStatus=1; //秒杀正在进行
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setUser(user);
        goodsDetailVo.setGoodsVo(goodsVo);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        return Result.success(goodsDetailVo);

    }
}
