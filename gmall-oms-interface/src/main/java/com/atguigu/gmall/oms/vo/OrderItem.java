package com.atguigu.gmall.oms.vo;

import com.itguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.itguigu.gmall.sms.vo.SkuSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItem {
    private Long skuId; //商品id
    private String title; //标题
    private String defaultImage; //图片
    private BigDecimal price; //数据库价格
    private Integer count; //商品数量
    private List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList; //商品销售属性
    private List<SkuSaleVO> skuSaleVO; //商品积分
    private BigDecimal weight;
    private Boolean store;

}
