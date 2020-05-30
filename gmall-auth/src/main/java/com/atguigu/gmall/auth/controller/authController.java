package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
@EnableConfigurationProperties(JwtProperties.class)
public class authController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredit")
    public Resp<Object> authentication(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletRequest request,
            HttpServletResponse Response
    ){
        //登录校验
        String tocken = this.authService.authentication(username,password);

        if (StringUtils.isBlank(tocken)){
            return Resp.fail("登录失败，用户名或密码错误");
        }
        //将tocken写入cookie,并指定httpOnly为true,防止通过JS获取和修改

        CookieUtils.setCookie(request,Response,jwtProperties.getCookieName(),tocken,jwtProperties.getExpire());

        return Resp.ok("登录成功！");
    }


}
