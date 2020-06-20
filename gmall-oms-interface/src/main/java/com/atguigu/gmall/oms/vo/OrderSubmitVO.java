package com.atguigu.gmall.oms.vo;


import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class OrderSubmitVO {
    private String orderToken; //防重唯一标志
    private MemberReceiveAddressEntity address;
    private Integer payType; //支付方式
    private String deliveryCompany; //物流信息

    private List<OrderItem> items; //购物车商品信息
    private Integer bounds; //积分
    private BigDecimal totalPrice; //总价
    private Long userId; //用户id

}
