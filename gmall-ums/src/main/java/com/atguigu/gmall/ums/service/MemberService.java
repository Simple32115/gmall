package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员
 *
 * @author liangwenhao
 * @email lwh@atguigu.com
 * @date 2020-05-29 15:46:26
 */
public interface MemberService extends IService<MemberEntity> {

    PageVo queryPage(QueryCondition params);

    Boolean queryData(String data, Integer type);

    void registry(MemberEntity memberEntity, String code);


    MemberEntity queryUser(String username, String password);
}

