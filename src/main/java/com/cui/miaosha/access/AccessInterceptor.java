package com.cui.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.redis.AccessKey;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.result.CodeMsg;
import com.cui.miaosha.result.Result;
import com.cui.miaosha.service.UserService;
import com.cui.miaosha.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            User user = getUser(request, response);
            UserContext.setUser(user);

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();

            if (needLogin) {
                if (user == null) {
                    render(response, CodeMsg.SERVER_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            } else {
                //dosomething
            }
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if (count == null) { //第一次访问
                redisService.set(ak, key, 1);
            } else if (count < maxCount) {
                redisService.incr(ak, key); //访问未超过限制次数，可以再次访问，同时访问加1
            } else {//在有效时间内访问次数达到上限，不能再访问
                render(response,CodeMsg.ACCESS_LIMIT);
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg serverError) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String string = JSON.toJSONString(Result.error(serverError));
        out.write(string.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private User getUser(HttpServletRequest request,HttpServletResponse response) {
        String paramToken = request.getParameter(UserServiceImpl.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,UserServiceImpl.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken; //paramToken优先于cookieToken
        User user = userService.getByToken(token,response);
        return user;
    }
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
