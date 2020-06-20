package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author liangwenhao
 * @email lwh@atguigu.com
 * @date 2020-06-11 11:01:30
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
    List<WareSkuEntity> checkStore(@Param("skuId") Long skuId, @Param("count") Integer count);

    int lockDao(@Param("id") Long id,@Param("count") Integer count);

    int unLockStore(@Param("wareSkuId") Long wareSkuId,@Param("count") Integer count);

}
