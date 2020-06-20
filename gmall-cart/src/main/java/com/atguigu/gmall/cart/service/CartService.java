package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.Entity.Cart;

import java.util.List;

public interface CartService {
    void addCart(Cart cart);

    List<Cart> queryCarts();

    List<Cart> updateCarts(Cart cart);

    void deleteCart(Long skuId);

    List<Cart> queryCheckCartByUserId(Long userId);
}
