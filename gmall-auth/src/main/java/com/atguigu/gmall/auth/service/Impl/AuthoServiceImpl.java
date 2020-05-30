package com.atguigu.gmall.auth.service.Impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthoServiceImpl implements AuthService {
    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public String authentication(String username, String password) {
        try {
            //调用服务，执行查询
            Resp<MemberEntity> queryUser = this.umsClient.queryUser(username, password);
            MemberEntity memberEntity = queryUser.getData();

            //如果查询结果为null，直接返回null
            if (memberEntity == null){
                return null;
            }
            //如果有查询结果，则生成token
            Map<String,Object> map = new HashMap<>();
            map.put("id",memberEntity.getId());
            map.put("username",memberEntity.getUsername());

            String token = JwtUtils.generateToken(map,jwtProperties.getPrivateKey(),jwtProperties.getExpire());
            return token;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
}
