package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品库存
 *
 * @author liangwenhao
 * @email lwh@atguigu.com
 * @date 2020-06-11 11:01:30
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageVo queryPage(QueryCondition params);

    String checkAndLockStore(List<SkuLockVO> skuLockVos);
}

