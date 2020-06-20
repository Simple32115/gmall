package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.order.pay.AlipayTemplate;
import com.atguigu.gmall.order.pay.PayVo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO submitVO){
        try {
            OrderEntity orderEntity = this.orderService.submit(submitVO);
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setTotal_amount(orderEntity.getPayAmount() != null ? orderEntity.getPayAmount().toString(): "100");
            payVo.setSubject("谷粒商城");
            payVo.setBody("支付平台");
            String pay = this.alipayTemplate.pay(payVo);
            System.out.println(pay);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return Resp.ok(null);
    }
    @PostMapping("pay/success")
    public Resp<Object> paySuccess(){

        return Resp.ok(null);

    }




    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm (){
        OrderConfirmVO orderConfirmVO = this.orderService.confirm();

        return Resp.ok(orderConfirmVO);
    }


}
