package com.atguigu.gmall.sms.service;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itguigu.gmall.sms.vo.SkuSaleDTO;
import com.itguigu.gmall.sms.vo.SkuSaleVO;

import java.util.List;


/**
 * 商品sku积分设置; InnoDB free: 5120 kB
 *
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-29 11:02:57
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSkuSaleInfo(SkuSaleDTO skuSaleDTO);

    List<SkuSaleVO> querySaleBySkuId(Long skuId);
}

