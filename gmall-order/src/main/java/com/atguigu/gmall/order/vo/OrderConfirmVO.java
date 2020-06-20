package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItem;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVO {
    //收货地址 ， ums_member_receive_address表
    private List<MemberReceiveAddressEntity> addresses;

    //购物清单，根据购物车传递过来的skuIds 查询
    private List<OrderItem> orderItems;

    //可用积分，，ums_member表中的integration字段
    private Integer bounds;

    //订单令牌
    private String orderToken;


}
