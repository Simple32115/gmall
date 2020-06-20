package com.atguigu.gmall.cart.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClinet;
import com.itguigu.gmall.pms.entity.SkuInfoEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CartListener {
    @Autowired
    private GmallPmsClinet gmallPmsClinet;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRICE_PREFIX = "gmall:cart";

    private static final String CART_PREFIX = "cart:uid:";


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "CART-ITEM-QUEUE" , durable = "ture"),
        exchange = @Exchange(value = "GMALL-PMS-EXCHANG",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
        key = {"item.update"}
    ))
    public void listener(Long spuId){
        Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClinet.querySkuBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
        skuInfoEntities.forEach(skuInfoEntity -> {
            this.redisTemplate.opsForValue().set(PRICE_PREFIX +skuInfoEntity.getSkuId(),skuInfoEntity.getPrice().toString());
        });

    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-CART-QUEUE",durable = "true"),
            exchange = @Exchange(value = "GMALL-ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void deleteListener(Map<String,Object> map){
        Long userId = (Long)map.get("userId");
        List<Object> skuIds = (List<Object>)map.get("skuIds");
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(CART_PREFIX + userId);
        List<String> skus = skuIds.stream().map(skuId -> skuId.toString()).collect(Collectors.toList());
        String[] ids = skus.toArray(new String[skus.size()]);
        hashOperations.delete(ids);

    }



}
