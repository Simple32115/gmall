package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.atguigu.gmall.oms.feign.GmallPmsClient;
import com.atguigu.gmall.oms.feign.GmallUmsClient;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.oms.vo.OrderItem;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguigu.gmall.pms.entity.SkuInfoEntity;
import com.itguigu.gmall.pms.entity.SpuInfoEntity;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private GmallUmsClient gmallUmsClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private OrderItemDao itemDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Override
    @Transactional
    public OrderEntity saveOrder(OrderSubmitVO submitVO) {
        //保存orderEntity
        MemberReceiveAddressEntity address = submitVO.getAddress();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverCity(address.getCity());
        Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryMemberByUserId(submitVO.getUserId());
        MemberEntity memberEntity = memberEntityResp.getData();
        orderEntity.setMemberUsername(memberEntity.getUsername());
        orderEntity.setMemberId(submitVO.getUserId());
        //清算每个商品赠送积分
        orderEntity.setIntegration(0);
        orderEntity.setGrowth(0);
        orderEntity.setStatus(0);

        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setDeliveryCompany(submitVO.getDeliveryCompany());
        orderEntity.setOrderSn(submitVO.getOrderToken());

        Long orderId = orderEntity.getId();

        this.save(orderEntity);

        //保存订单详情OrderItemEntity
        List<OrderItem> items = submitVO.getItems();
        items.forEach(orderItem -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setSkuId(orderItem.getSkuId());

            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuBySkuId(orderItem.getSkuId());
            SkuInfoEntity skuEntity = skuInfoEntityResp.getData();

            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuBySpuId(skuEntity.getSpuId());
            SpuInfoEntity spuEntity = spuInfoEntityResp.getData();

            orderItemEntity.setSkuName(skuEntity.getSkuName());
            orderItemEntity.setSkuPic(skuEntity.getSkuDefaultImg());
            orderItemEntity.setSkuQuantity(orderItem.getCount());
            orderItemEntity.setSkuAttrsVals(JSON.toJSONString(orderItem.getSkuSaleAttrValueEntityList()));
            orderItemEntity.setSkuPrice(orderItem.getPrice());

            orderItemEntity.setCategoryId(spuEntity.getCatalogId());
            orderItemEntity.setOrderId(orderId);
            orderItemEntity.setOrderSn(submitVO.getOrderToken());
            orderItemEntity.setSpuId(spuEntity.getId());

            this.itemDao.insert(orderItemEntity);
        });

        //int i=  1/0;
        //创建订单之后，在响应之前发送延时消息，达到定时关单的效果
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","order.ttl",submitVO.getOrderToken());
        return orderEntity;
    }

}


