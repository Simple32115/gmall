package com.atguigu.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.Entity.Cart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallCartApi {
    @GetMapping("cart/query/Cart/{userId}")
    public Resp<List<Cart>> queryCheckCartByUserId(@PathVariable("userId")Long userId);
}
