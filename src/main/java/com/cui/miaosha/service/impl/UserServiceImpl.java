package com.cui.miaosha.service.impl;

import com.cui.miaosha.dao.UserDao;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.exception.GlobalException;
import com.cui.miaosha.redis.RedisService;
import com.cui.miaosha.redis.UserKey;
import com.cui.miaosha.result.CodeMsg;
import com.cui.miaosha.service.UserService;
import com.cui.miaosha.utils.MD5Utils;
import com.cui.miaosha.utils.UUIDUtil;
import com.cui.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserServiceImpl implements UserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private UserDao userDao;
    @Autowired
    private RedisService redisService;
    @Override
    public boolean login(LoginVo loginVo, HttpServletResponse response) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        Long id = Long.parseLong(mobile);
        User user = getById(id); //手机号作为id查找User对象
        if (user == null) {
//            return CodeMsg.MOBILE_NOTEXIST;
            throw new GlobalException(CodeMsg.MOBILE_NOTEXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calPass = MD5Utils.formPassToDBPass(formPass, saltDB);
        if (!calPass.equals(dbPass)) {
//            return CodeMsg.PASSWORD_ERROR;
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        //登录成功后，生成cookie,并存入redis缓存中
        String token = UUIDUtil.uuid();
        addCookie(token,user,response);

        return true;
    }
    private void addCookie(String token,User user,HttpServletResponse response) {
        redisService.set(UserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(UserKey.token.expireSeconds()); //cookie的年龄和redis中key的年龄一致
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    public User getByToken(String token,HttpServletResponse response) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        User user = redisService.get(UserKey.token, token, User.class);
        //延长缓存期
        if (user != null) {
            addCookie(token,user,response);
        }
        return user;

    }

    /**
     * 根据手机号（id）查找User对象
     * @param id
     * @return
     */
    public User getById(Long id) {
        //取缓存
        User user = redisService.get(UserKey.getById, ""+id, User.class);
        if (user != null) {
            return user;
        }
        //取数据库
        user = userDao.findUserByMobile(id);
        if (user != null) {
            redisService.set(UserKey.getById, ""+id,user);//写入缓存
        }

        return user;
    }

    /**
     * 修改用户密码
     */
    public boolean updatePassword(String token,Long id, String formPass) {
        User user = getById(id);
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOTEXIST);
        }
        //更新数据库
        User toBeUpdate = new User();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Utils.formPassToDBPass(formPass, user.getSalt()));
        userDao.update(toBeUpdate);
        //数据库更新后，修改缓存
        redisService.delete(UserKey.getById, id.toString()); //删除旧id对应的user
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(UserKey.token, token, user); //更新token对应的user
        return true;
    }
}
