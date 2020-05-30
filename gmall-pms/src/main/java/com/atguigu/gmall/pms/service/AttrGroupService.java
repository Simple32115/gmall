package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.itguigu.gmall.pms.vo.ItemGroupVO;

import java.util.List;


/**
 * 属性分组; InnoDB free: 5120 kB
 *
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-26 21:35:35
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroupByPage(QueryCondition condition, Long catId);

    AttrGroupVO queryByGroupId(Long gid);

    List<AttrGroupVO> queryByCid(Long cid);

    List<ItemGroupVO> queryItemGroupBySpuIdCatId(Long spuId, Long catalogId);
}

