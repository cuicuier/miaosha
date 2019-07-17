package com.cui.miaosha.service;

import com.cui.miaosha.domain.User;
import com.cui.miaosha.vo.LoginVo;

import javax.servlet.http.HttpServletResponse;

public interface UserService {

    boolean login(LoginVo loginVo, HttpServletResponse response);


    User getByToken(String token,HttpServletResponse response);
}
