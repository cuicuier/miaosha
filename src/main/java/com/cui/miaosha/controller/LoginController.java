package com.cui.miaosha.controller;

import com.cui.miaosha.domain.User;
import com.cui.miaosha.result.CodeMsg;
import com.cui.miaosha.result.Result;
import com.cui.miaosha.service.UserService;
import com.cui.miaosha.utils.ValidatorUtils;
import com.cui.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    /**
     * 做登录操作
     * @return
     */
    @RequestMapping("/do_login")
    @ResponseBody
    public Result doLogin(@Valid LoginVo loginVo, HttpServletResponse response) {
        log.info(loginVo.toString()); //打印参数信息
        //参数校验
        /*String passInput = loginVo.getPassword();  //密码经过了客户端第一次MD5加密
        String mobile = loginVo.getMobile();
        if (StringUtils.isEmpty(passInput)) {
            return Result.error(CodeMsg.PASSWORD_EMPTY);
        }
        if (StringUtils.isEmpty(mobile)) {
            return Result.error(CodeMsg.MOBILE_EMPTY);
        }
        //判断手机号的格式
        if (!ValidatorUtils.isMobile(mobile)) {
            return Result.error(CodeMsg.MOBILE_ERROR);
        }*/

        //登录(手机号作为用户的id)
        userService.login(loginVo,response);  //若有异常则抛出
        return Result.success(true);

    }

    /**
     * 跳转到登录页面
     * @return
     */
    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

}
