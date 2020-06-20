package atguigu.gmallsearch;

import atguigu.gmallsearch.feign.GmallPmsClient;
import atguigu.gmallsearch.feign.GmallWmsClient;
import atguigu.gmallsearch.pojo.Goods;
import atguigu.gmallsearch.pojo.SearchAttr;
import atguigu.gmallsearch.repository.GoodsRepository;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.itguigu.gmall.pms.entity.*;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Test
    void contextLoads() {
        this.elasticsearchRestTemplate.createIndex(Goods.class);
        this.elasticsearchRestTemplate.putMapping(Goods.class);
    }

    @Test
    void importData() {

        Long pageNum = 1L;
        Long pageSize = 100L;

        do {
            //分页查询spu
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> resp = this.gmallPmsClient.querySpusByPage(queryCondition);
            List<SpuInfoEntity> spus  = resp.getData();
            if (spus  == null){
                continue;
            }

            //遍历spu 查询sku
            spus .forEach(spuInfoEntity -> {
                //查询里面的数据
                Resp<List<SkuInfoEntity>> listResp = this.gmallPmsClient.querySkuBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntityList = listResp.getData();
                //如果集合非空再把sku转化为goods对象
                if (!CollectionUtils.isEmpty(skuInfoEntityList)) {
                    //把sku转化成goods对象
                    List<Goods> goodsList = skuInfoEntityList.stream().map(skuInfoEntity -> {
                        Goods goods = new Goods();
                        //查询搜索属性和值
                        Resp<List<ProductAttrValueEntity>> attrValueResp = this.gmallPmsClient.findProBySpuId(skuInfoEntity.getSpuId());
                        List<ProductAttrValueEntity> attrValueEntityList = attrValueResp.getData();
                        if (!CollectionUtils.isEmpty(attrValueEntityList)) {
                            List<SearchAttr> searchAttrList = attrValueEntityList.stream().map(productAttrValueEntity -> {
                                SearchAttr searchAttr = new SearchAttr();
                                searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                                searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                                searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());

                                return searchAttr;
                            }).collect(Collectors.toList());
                            goods.setAttrs(searchAttrList);
                        }

                        //查询品牌
                        Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandByBrandId(skuInfoEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResp.getData();
                        if (brandEntity != null) {
                            goods.setBrandId(skuInfoEntity.getBrandId());
                            goods.setBrandName(brandEntity.getName());
                        }
                        //查询分类
                        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryByCatId(skuInfoEntity.getCatalogId());
                        CategoryEntity categoryEntity = categoryEntityResp.getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(skuInfoEntity.getCatalogId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        goods.setCreateTime(spuInfoEntity.getCreateTime());

                        goods.setPic(skuInfoEntity.getSkuDefaultImg());
                        goods.setPrice(skuInfoEntity.getPrice());
                        goods.setSale(0l);
                        goods.setSkuId(skuInfoEntity.getSkuId());

                        //查询库存信息
                        Resp<List<WareSkuEntity>> queryWareSkusBySkuId = this.gmallWmsClient.queryWareByskuId(skuInfoEntity.getSkuId());
                        List<WareSkuEntity> wareSkuEntityList = queryWareSkusBySkuId.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntityList)) {
                            //list有一个对象有库存既有货
                            boolean flag = wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                            goods.setStore(flag);
                        }

                        goods.setTitle(skuInfoEntity.getSkuTitle());

                        return goods;
                    }).collect(Collectors.toList());

                    this.goodsRepository.saveAll(goodsList);
                }
            });


            //导入索引库

            pageSize = (long) spus.size();
            pageNum++;

        } while (pageSize == 100);
    }

}
