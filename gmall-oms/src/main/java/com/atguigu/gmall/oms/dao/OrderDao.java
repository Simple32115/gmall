package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 *
 * @author weige
 * @email zww@atguigu.com
 * @date 2020-06-12 10:59:46
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
    public int closeOrder(String orderToken);

}
