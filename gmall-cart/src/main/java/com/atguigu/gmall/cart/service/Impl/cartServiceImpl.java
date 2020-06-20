package com.atguigu.gmall.cart.service.Impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.Entity.Cart;
import com.atguigu.gmall.cart.Entity.UserInfo;
import com.atguigu.gmall.cart.feign.GmallSmsClinet;
import com.atguigu.gmall.cart.feign.GmallWmsClinet;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.itguigu.gmall.pms.api.GmallPmsApi;
import com.itguigu.gmall.pms.entity.SkuInfoEntity;
import com.itguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.itguigu.gmall.sms.vo.SkuSaleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class cartServiceImpl implements CartService {
    @Autowired
    private GmallPmsApi gmallPmsApi;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallSmsClinet gmallSmsClinet;

    @Autowired
    private GmallWmsClinet gmallWmsClinet;

    private static final String CART_PREFIX = "cart:uid:";
    private static final String PRICE_PREFIX = "gmall:cart";

    @Override
    public void addCart(Cart cart) {
        //获取userInfo
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //获取redis中的key
        String key = CART_PREFIX;
        if (userInfo.getId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getId();
        }
        //查询用户的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);


        //判断购物车是否存在
        Long skuId = cart.getSkuId();
        Integer count = cart.getCount();


        //注意这里的skuId要转化成String，因为redis中保存的都是String
        if (hashOps.hasKey(skuId.toString())) {
            //购物车已存在该记录，更新数量
            //获取购物车中的sku记录
            String cartJson = hashOps.get(skuId.toString()).toString();
            //反序列化，更新数量
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount() + count);


        } else {
            //没有 新增购物车记录

            cart.setCheck(true);
            Resp<SkuInfoEntity> entityResp = this.gmallPmsApi.querySkuBySkuId(skuId);
            SkuInfoEntity sku = entityResp.getData();
            cart.setDefaultImage(sku.getSkuDefaultImg());
            cart.setPrice(sku.getPrice());

            Resp<List<SkuSaleAttrValueEntity>> resp = this.gmallPmsApi.querySkuSaleBySkuId(skuId);
            List<SkuSaleAttrValueEntity> skuSale = resp.getData();
            cart.setSkuSaleAttrValueEntityList(skuSale);

            Resp<List<SkuSaleVO>> listResp = this.gmallSmsClinet.querySaleBySkuId(skuId);
            List<SkuSaleVO> skuSaleVOS = listResp.getData();
            cart.setSkuSaleVO(skuSaleVOS);

            Resp<List<WareSkuEntity>> listResp1 = this.gmallWmsClinet.queryWareByskuId(skuId);
            List<WareSkuEntity> skuware = listResp1.getData();

            //库存可能为null，如果为null则不用判断
            if (!CollectionUtils.isEmpty(skuSale)) {
                cart.setStore(skuware.stream().anyMatch(skuSaleVO -> skuSaleVO.getStock() > 0));
            }
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId,sku.getPrice().toString());
        }
        // 重新写入redis
        hashOps.put(skuId.toString(), JSON.toJSONString(cart));
    }

    @Override
    public List<Cart> queryCarts() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查询未登录状态下购物车
        List<Cart> unCarts = null;
        String userKey = CART_PREFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(userKey);
        List<Object> cartJsonList = userKeyOps.values();
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            unCarts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String priceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(priceString));
                return cart;
            }).collect(Collectors.toList());
        }
        //判断用户是否登录
        if (userInfo.getId()==null){
            return unCarts;
        }
        //用户已登录，查询登录状态的购物车
        String key = CART_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> carts = this.redisTemplate.boundHashOps(key);

        //如果未登录状态的购物车不为空，需要合并
        if (!CollectionUtils.isEmpty(unCarts)){
            //合并购物车
            unCarts.forEach(unCart -> {
                Long skuId = unCart.getSkuId();
                Integer count = unCart.getCount();
                if (carts.hasKey(skuId.toString())){
                    //购物车已存在该记录，更新记录
                    String cartJson = carts.get(skuId.toString()).toString();
                    Cart cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount() + count);
                }
                //购物车不存在该记录，更新数量
                carts.put(skuId.toString(),JSON.toJSONString(unCart));
            });
        //合并完成之后，删除未登录的购物车
            this.redisTemplate.delete(userKey);
        }
        //查询登录状态的购物车
        List<Object> values = carts.values();
        return values.stream().map(value -> {
            Cart cart = JSON.parseObject(value.toString(), Cart.class);
            //查询当前价格
            String priceString  = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
            cart.setCurrentPrice(new BigDecimal(priceString));
            return cart;

        }).collect(Collectors.toList());
    }

    @Override
    public List<Cart> updateCarts(Cart cart) {
        //获取登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //获取redis的key
        //获取redis中的key
        String key = CART_PREFIX;
        if (userInfo.getId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getId();
        }
        //获取购物车对象
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(key);
        String skuId = cart.getSkuId().toString();

        if (boundHashOps.hasKey(skuId)){
            //获取购物车信息
            String cartJson = boundHashOps.get(skuId).toString();
            Integer count = cart.getCount();
            Cart cart1 = JSON.parseObject(cartJson, Cart.class);
            //更新数量
            cart1.setCount(count);
            //写入购物车
            boundHashOps.put(cart1.getSkuId().toString(),JSON.toJSONString(cart1));
        }


        return null;
    }

    @Override
    public void deleteCart(Long skuId) {
        //获取登录参数
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //获取redis中的key
        String key = CART_PREFIX;
        if(userInfo.getId() == null){
            key += userInfo.getUserKey();
        }else {
            key += userInfo.getId();
        }

        //获取购物车对象
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
       hashOperations.delete(skuId.toString());

    }

    @Override
    public List<Cart> queryCheckCartByUserId(Long userId) {
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(CART_PREFIX + userId);
        List<Object> cartJsonList = hashOperations.values();
        List<Cart> carts = cartJsonList.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).filter(Cart::getCheck).collect(Collectors.toList());
        return carts;

    }
}
