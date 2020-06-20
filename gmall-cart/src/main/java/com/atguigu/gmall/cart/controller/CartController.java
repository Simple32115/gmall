package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.Entity.Cart;
import com.atguigu.gmall.cart.Entity.UserInfo;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("query/Cart/{userId}")
    public Resp<List<Cart>> queryCheckCartByUserId(@PathVariable("userId")Long userId){
        List<Cart> carts = this.cartService.queryCheckCartByUserId(userId);

        return Resp.ok(carts);
    }

    @PostMapping("{skuId}")
    public Resp<Object> deleteCart(@PathVariable("skuId") Long skuId){
        this.cartService.deleteCart(skuId);

        return Resp.ok(null);
    }

    @GetMapping("query")
    public Resp<List<Cart>> queryCarts(){
        List<Cart> carts = this.cartService.queryCarts();

        return Resp.ok(carts);
    }
    @PostMapping("update")
    public Resp<List<Cart>> updateCarts(@RequestBody Cart cart){

        List<Cart> carts=this.cartService.updateCarts(cart);

        return Resp.ok(null);
    }

    @PostMapping("cart")
    public Resp<Object> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);

        return Resp.ok(null);
    }

    @GetMapping("test")
    public String test(){
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        return "hello cart!";
    }


}
