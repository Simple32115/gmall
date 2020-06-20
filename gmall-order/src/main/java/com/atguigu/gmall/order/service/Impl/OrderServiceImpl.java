package com.atguigu.gmall.order.service.Impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.cart.Entity.Cart;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.oms.vo.OrderItem;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.itguigu.gmall.pms.entity.SkuInfoEntity;
import com.itguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private GmallUmsClient gmallUmsApi;

    @Autowired
    private GmallPmsClinet gmallPmsApi;

    @Autowired
    private GmallSmsClinet gmallSmsClinet;

    @Autowired
    private GmallWmsClinet gmallWmsApi;

    @Autowired
    private GmallCartClient gmallCartClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "order:token";

    @Autowired
    private GmallOmsClient gmallOmsClient;

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Override
    public OrderConfirmVO confirm() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getId();
        if (userId == null){
            return  null;
        }

        //获取用户的收货地址列表，根据用户id查询收货地址列表
        Resp<List<MemberReceiveAddressEntity>> listResp = this.gmallUmsApi.queryAddressByUserId(userId);
        List<MemberReceiveAddressEntity> addressEntities = listResp.getData();
        orderConfirmVO.setAddresses(addressEntities);
        //获取购物车中的商品信息(已选中的商品)
        Resp<List<Cart>> cartsResp = this.gmallCartClient.queryCheckCartByUserId(userId);
        List<Cart> carts = cartsResp.getData();

        if (CollectionUtils.isEmpty(carts)){
           throw new OrderException("请勾选商品！");
        }
        List<OrderItem> itemVOs = carts.stream().map(cart -> {
            OrderItem orderItem = new OrderItem();
            Long skuId = cart.getSkuId();
            orderItem.setSkuId(skuId);
            Resp<SkuInfoEntity> skuInfoEntityResp =
                    this.gmallPmsApi.querySkuBySkuId(skuId);
            SkuInfoEntity sku = skuInfoEntityResp.getData();
            if (sku != null) {
                orderItem.setCount(cart.getCount());
                orderItem.setDefaultImage(sku.getSkuDefaultImg());
                orderItem.setPrice(sku.getPrice());
                orderItem.setTitle(sku.getSkuTitle());
                orderItem.setWeight(sku.getWeight());
            }
            Resp<List<SkuSaleAttrValueEntity>> skuSaleResp =
                    this.gmallPmsApi.querySkuSaleBySkuId(skuId);
            List<SkuSaleAttrValueEntity> skuSale =
                    skuSaleResp.getData();
            if (skuSale != null) {
                orderItem.setSkuSaleAttrValueEntityList(skuSale);
            }
            Resp<List<WareSkuEntity>> skuWareResp = this.gmallWmsApi.queryWareByskuId(skuId);
            List<WareSkuEntity> skuWare = skuWareResp.getData();
            if (!CollectionUtils.isEmpty(skuWare)) {
                orderItem.setStore(skuWare.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
            return orderItem;
        }).collect(Collectors.toList());
        orderConfirmVO.setOrderItems(itemVOs);

        //查询用户信息，获取积分
        Resp<MemberEntity> memberEntityResp = this.gmallUmsApi.queryMemberByUserId(userId);
        MemberEntity memberEntity = memberEntityResp.getData();
        orderConfirmVO.setBounds(memberEntity.getIntegration());

        //生成一个唯一标志，防止重复提交(响应到页面有一份,有一份保存到redis中)
        String orderToken = IdWorker.getIdStr();
        orderConfirmVO.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(TOKEN_PREFIX + orderToken ,orderToken);
        return orderConfirmVO;
    }

    @Override
    public OrderEntity submit(OrderSubmitVO submitVO) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //1.防重复提交，查询redis中有没有ordertoken信息，有，则是第一次提交，放行并删除reids中的ordertoken信息
        String orderToken = submitVO.getOrderToken();
        //释放锁,其他才可以拿到锁(lua脚本)
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
        if (flag == 0){
            throw new OrderException("订单不可提交!");
        }
        //2.校验价格，总价一直放行
        List<OrderItem> items = submitVO.getItems(); //送货清单
        BigDecimal totalPrice = submitVO.getTotalPrice(); //总价

        if (CollectionUtils.isEmpty(items)){
            throw new OrderException("请勾选商品后在提交订单！");
        }
        //获取送货清单里所有商品价格之和
        BigDecimal currentTotalPrice = items.stream().map(orderItem -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsApi.querySkuBySkuId(orderItem.getSkuId());
            SkuInfoEntity sku = skuInfoEntityResp.getData();
            if (sku != null) {
                return sku.getPrice().multiply(new BigDecimal(orderItem.getCount()));
            }
            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();
        //判断购物车价格和数据库价格是否一致
        if (currentTotalPrice.compareTo(totalPrice) != 0){
            throw new OrderException("页面已过期，请刷新页面后重新下单！");
        }

        //3.校验库存是否充足并锁定库存，一次性提示所有库存不够的商品信息
        List<SkuLockVO> skuLockVOS = items.stream().map(orderItem -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItem.getSkuId());
            skuLockVO.setCount(orderItem.getCount());
            skuLockVO.setOrderToken(orderToken);
            return skuLockVO;
        }).collect(Collectors.toList());

        Resp<Object> wareResp = this.gmallWmsApi.checkAndLockStore(skuLockVOS);
        if (wareResp.getCode() != 0){
            throw new OrderException(wareResp.getMsg());
        }
        //int i = 1/0;

        //4.下单（创建订单及订单详情，远程接口待开发）

        Resp<OrderEntity> orderEntityResp = null;
        try {
            submitVO.setUserId(userInfo.getId());
            orderEntityResp = this.gmallOmsClient.saveOrder(submitVO);
        } catch (Exception e) {
            e.printStackTrace();
            //发送消息解锁库存
            this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","wms.unlock",orderToken);
            throw new OrderException("服务器错误，创建订单失败");
        }

        //5.删除购物车(需要传两个参数过去，一个是用户id，还有要删除的skuId)
        Map<String,Object> map = new HashMap<>();
        map.put("userId", userInfo.getId());
        List<Long> skuIds = items.stream().map(OrderItem::getSkuId).collect(Collectors.toList());
        map.put("skuIds",skuIds);
        //发送消息，购物车微服务接收消息在删除已选定的购物车
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","cart.delete",map);

        if (orderEntityResp != null){
            return orderEntityResp.getData();
        }

        return null;

    }

}
