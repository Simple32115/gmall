package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.feign.SkuSaleFeign;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.service.SpuInfoService;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguigu.gmall.pms.entity.*;
import com.itguigu.gmall.sms.vo.SkuSaleDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao,
        SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;

    @Autowired
    private SkuSaleFeign skuSaleFeign;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Value("${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryspuInfo(QueryCondition condition, Long catId) {

        QueryWrapper<SpuInfoEntity> queryWrapper =
                new QueryWrapper<>();

        if (catId != 0) {
            queryWrapper.eq("catalog_id", catId);
        }

        // 如果用户输入了检索条件，根据检索条件查
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.and(t -> t.eq("id", key).or().like(
                    "spu_name", key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                queryWrapper
        );


        return new PageVo(page);
    }
    //开启全局事务
    @Override
    public void saveSpuInfoVO(SpuInfoVO spuInfoVO) {
        /// 1.保存spu相关
        // 1.1. 保存spu基本信息 spu_info
        spuInfoVO.setPublishStatus(1); // 默认是已上架
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime()); //
        // 新增时，更新时间和创建时间一致
        this.save(spuInfoVO);
        Long spuId = spuInfoVO.getId(); // 获取新增后的spuId

        // 1.2. 保存spu的描述信息 spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        // 注意：spu_info_desc表的主键是spu_id,需要在实体类中配置该主键不是自增主键
        spuInfoDescEntity.setSpuId(spuId);
        // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
        spuInfoDescEntity.setDecript(StringUtils.join(spuInfoVO.getSpuImages(), ","));
        this.spuInfoDescDao.insert(spuInfoDescEntity);

        // 1.3. 保存spu的规格参数信息
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueEntities =
                    baseAttrs.stream().map(productAttrValueVO -> {
                        productAttrValueVO.setSpuId(spuId);
                        productAttrValueVO.setAttrSort(0);
                        productAttrValueVO.setQuickShow(0);
                        return productAttrValueVO;
                    }).collect(Collectors.toList());
            this.productAttrValueService.saveBatch(productAttrValueEntities);
        }

        /// 2. 保存sku相关信息
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(skuInfoVO -> {
            // 2.1. 保存sku基本信息 pms_sku_info
            skuInfoVO.setSpuId(spuId);
            skuInfoVO.setSkuCode(UUID.randomUUID().toString());
            skuInfoVO.setBrandId(spuInfoVO.getBrandId());
            skuInfoVO.setCatalogId(spuInfoVO.getCatalogId());
            List<String> images = skuInfoVO.getImages();
            //设置默认图片
            if (!CollectionUtils.isEmpty(images)) {
                skuInfoVO.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoVO.getSkuDefaultImg()) ? skuInfoVO.getSkuDefaultImg() : images.get(0));
            }
            this.skuInfoDao.insert(skuInfoVO);

            Long skuId = skuInfoVO.getSkuId();
            // 2.2. 保存sku图片信息 pms_sku_image
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> collect = images.stream().map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setImgUrl(image);
                            skuImagesEntity.setSkuId(skuId);
                            //设置是否默认图片
                            skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoVO.getSkuDefaultImg(), image) ? 1 : 0);
                            return skuImagesEntity;
                        }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(collect);
            }

            // 2.3. 保存sku的规格参数（销售属性）pms_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs =
                    skuInfoVO.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                //设置skuId
                saleAttrs.forEach(skuSaleAttrValueEntity -> skuSaleAttrValueEntity.setSkuId(skuId));
                //批量保存销售属性
                this.saleAttrValueService.saveBatch(saleAttrs);
            }
            SkuSaleDTO skuSaleDTO = new SkuSaleDTO();
            BeanUtils.copyProperties(skuInfoVO, skuSaleDTO);
            skuSaleDTO.setSkuId(skuId);
            this.skuSaleFeign.saveSkuSaleInfo(skuSaleDTO);
        });
            this.sendMsg("insert",spuId);
    }
    private void sendMsg(String type , Long spuId){
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME,"item."+ type,spuId);

    }

}
