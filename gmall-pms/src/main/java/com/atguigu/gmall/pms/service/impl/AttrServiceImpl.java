package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.itguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.itguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import com.atguigu.gmall.pms.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao,
        AttrEntity> implements AttrService {
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidTypePage(QueryCondition condition,
                                     Long cid, Integer type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        if (cid != null) {
            wrapper.eq("catelog_id", cid);
        }
        //因为设置了默认值，所以type不可能为空
        wrapper.eq("attr_type", type);

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(condition),
                wrapper
        );
        return new PageVo(page);
    }

    @Override
    @Transactional
    public void saveAttrVO(AttrVO attrVO) {
        //新增规格参数
        this.attrDao.insert(attrVO);

        AttrAttrgroupRelationEntity relationEntity =
                new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
        relationEntity.setAttrId(attrVO.getAttrId());
        this.relationDao.insert(relationEntity);
    }

}
