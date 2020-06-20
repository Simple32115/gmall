package com.atguigu.gmall.cart.interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.cart.Entity.UserInfo;
import com.atguigu.gmall.cart.config.JwtProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();


    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取cookie信息
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKeyName());


        //如果都为空，设置userkey。
        if (StringUtils.isBlank(token) && StringUtils.isBlank(userKey)){
            String userkey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request,response,jwtProperties.getUserKeyName(),userkey,jwtProperties.getExpire());
        }

        //不管有没有登录都要将userKey存入对象保存

        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);

        // token不为空，解析token
        if (StringUtils.isNotBlank(token)){
            Map<String, Object> fromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            if (!CollectionUtils.isEmpty(fromToken)){
                userInfo.setId(new Long(fromToken.get("id").toString()));
            }
        }
        //保存到threadlocal
        THREAD_LOCAL.set(userInfo);

        //如果token不为空
        return true;
    }
    //因为当前线程用完之后会将线程返回线程池，此时如果不释放线程则会造成内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       THREAD_LOCAL.remove();
    }
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();

    }

}
