package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性&属性分组关联; InnoDB free: 5120 kB
 *
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-26 21:35:35
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageVo queryPage(QueryCondition params);

    void deleteByArrIdArrGroupId(List<AttrAttrgroupRelationEntity> attrgroupRelationEntities);
}

