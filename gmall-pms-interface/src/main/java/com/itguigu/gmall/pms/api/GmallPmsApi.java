package com.itguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.itguigu.gmall.pms.entity.*;
import com.itguigu.gmall.pms.vo.CategoryVO;
import com.itguigu.gmall.pms.vo.ItemGroupVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    @GetMapping("pms/attrgroup/item/group/{spuId}/{catalogId}")
    public Resp<List<ItemGroupVO>> queryItemGroupBySpuIdCatId(@PathVariable("spuId")Long spuId , @PathVariable("catalogId")Long catalogId);

    @RequestMapping("pms/skuimages/{skuId}")
    public Resp<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> querySkuBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("pms/category/{pid}")
    public Resp<List<CategoryVO>> querySubCategoryByPid(@PathVariable("pid")Long pid);

    @RequestMapping("pms/category")
    public Resp<List<CategoryEntity>> queryCategoriesByPidOrLevel(@RequestParam(value = "level", defaultValue = "0") Integer level,
                                                                  @RequestParam(value = "parentCid", required = false) Long pid);
    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> querySpuBySpuId(@PathVariable("id") Long id);

    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpusByPage(@RequestBody QueryCondition queryCondition);


    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> querySpuInfoDescBySpuId(@PathVariable("spuId") Long spuId);


    @RequestMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);


    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandByBrandId(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryByCatId(@PathVariable("catId") Long catId);

    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> findProBySpuId(@PathVariable("spuId")Long spuId);
    @RequestMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySkuSaleBySpuId(@PathVariable("spuId")Long spuId);

}
