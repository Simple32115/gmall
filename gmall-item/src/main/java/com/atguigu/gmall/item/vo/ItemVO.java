package com.atguigu.gmall.item.vo;

import com.itguigu.gmall.pms.entity.BrandEntity;
import com.itguigu.gmall.pms.entity.CategoryEntity;
import com.itguigu.gmall.pms.entity.SkuImagesEntity;
import com.itguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.itguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情页
 */
@Data
public class ItemVO {

    //1.当前sku的基本信息
    private Long skuId;
    private Long spuId;
    private String spuName;
    private CategoryEntity catalogEntity;
    private BrandEntity brandEntity;
    private String skuTitle;
    private String skuSubtitle;
    private BigDecimal price;
    private BigDecimal weight;


    //2.sku的所有图片
    private List<SkuImagesEntity> pics;

    //3.sku的所有促销信息
    private List<SkuSaleVO> sales;

    private Boolean store; //是否有货

    //4.sku的所有销售属性集合
    private List<SkuSaleAttrValueEntity> saleAttrs;

    //5.spu海报
    private List<String> images;

    private List<ItemGroupVO> groups;  //规格参数以及规格参数下的规格参数值



}
