package com.atguigu.gmall.item.service.Impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallSmsClinet;
import com.atguigu.gmall.item.feign.GmallWmsClinet;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVO;
import com.itguigu.gmall.pms.api.GmallPmsApi;
import com.itguigu.gmall.pms.entity.*;
import com.itguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private GmallPmsApi gmallPmsApi;

    @Autowired
    private GmallSmsClinet gmallSmsClinet;

    @Autowired
    private GmallWmsClinet gmallWmsClinet;

    @Override
    public ItemVO queryItemBySkuId(Long skuId) {
        ItemVO itemVO = new ItemVO();
        Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsApi.querySkuBySkuId(skuId);
        if (skuInfoEntityResp == null){
            return itemVO;
        }

        SkuInfoEntity skuData = skuInfoEntityResp.getData();
        itemVO.setSkuId(skuId);
        itemVO.setSkuTitle(skuData.getSkuTitle());
        itemVO.setSkuSubtitle(skuData.getSkuSubtitle());
        itemVO.setPrice(skuData.getPrice());
        itemVO.setWeight(skuData.getWeight());

        Long spuId = skuData.getSpuId();
        Resp<SpuInfoEntity> resp = this.gmallPmsApi.querySpuBySpuId(spuId);
        SpuInfoEntity spuEntity = resp.getData();
        //根据sku中的spuId查询spu
        itemVO.setSpuId(spuId);
        if (spuEntity != null){
            itemVO.setSpuName(spuEntity.getSpuName());
        }

        //根据skuId查询图片列表
        Resp<List<SkuImagesEntity>> listResp = this.gmallPmsApi.querySkuImagesBySkuId(skuId);
        List<SkuImagesEntity> skuImages = listResp.getData();

        if (!CollectionUtils.isEmpty(skuImages)){
            itemVO.setPics(skuImages);
        }

        //根据sku中brandId和CategoryId查询品牌和分类
        Long brandId = skuData.getBrandId();
        Long catalogId = skuData.getCatalogId();

        Resp<BrandEntity> brandEntity = this.gmallPmsApi.queryBrandByBrandId(brandId);
        BrandEntity brand = brandEntity.getData();
        if (brand != null){
            itemVO.setBrandEntity(brand);
        }

        Resp<CategoryEntity> categoryEntity = this.gmallPmsApi.queryCategoryByCatId(catalogId);
        CategoryEntity category = categoryEntity.getData();
        if (category != null){
            itemVO.setCatalogEntity(category);
        }


        //根据skuId查询营销信息
        Resp<List<SkuSaleVO>> sales = this.gmallSmsClinet.querySaleBySkuId(skuId);
        List<SkuSaleVO> skuSaleVOS = sales.getData();

        itemVO.setSales(skuSaleVOS);

        //根据skuId查询库存信息
        Resp<List<WareSkuEntity>> listResp1 = this.gmallWmsClinet.queryWareByskuId(skuId);
        List<WareSkuEntity> wareSkuEntities = listResp1.getData();
        itemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));

        //根据spuId 查询在查询所有的销售属性
        Resp<List<SkuSaleAttrValueEntity>> listResp2 = this.gmallPmsApi.querySkuSaleBySpuId(spuId);
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = listResp2.getData();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueEntities)){
            itemVO.setSaleAttrs(skuSaleAttrValueEntities);
        }
        //根据spuId查询商品描述（海报)
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsApi.querySpuInfoDescBySpuId(spuId);
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescEntityResp.getData();
        if (spuInfoDescEntity != null){

            String decript = spuInfoDescEntity.getDecript();
            String[] split = StringUtils.split(decript, ",");

            itemVO.setImages(Arrays.asList(split));
        }


        //根据spuId和cateId 查询组及其组下规格参数值
        Resp<List<ItemGroupVO>> listResp3 = this.gmallPmsApi.queryItemGroupBySpuIdCatId(spuId, catalogId);
        List<ItemGroupVO> itemGroupVOS = listResp3.getData();
        itemVO.setGroups(itemGroupVOS);


        return itemVO;
    }
}
