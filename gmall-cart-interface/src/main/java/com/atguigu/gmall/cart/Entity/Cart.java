package com.atguigu.gmall.cart.Entity;

import com.itguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.itguigu.gmall.sms.vo.SkuSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {
    private Long skuId; //商品id
    private String title; //标题
    private String defaultImage; //图片
    private BigDecimal price; //加入购物车时的价格
    private BigDecimal CurrentPrice; //当前商品价格
    private Integer count; //商品数量
    private Boolean store; //是否有货
    private List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList; //商品销售属性
    private List<SkuSaleVO> skuSaleVO; //商品积分
    private Boolean check; //进入购物车是否勾选

}
