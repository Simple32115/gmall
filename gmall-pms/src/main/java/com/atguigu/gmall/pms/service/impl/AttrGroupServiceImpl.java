package com.atguigu.gmall.pms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.itguigu.gmall.pms.entity.AttrEntity;
import com.itguigu.gmall.pms.entity.AttrGroupEntity;
import com.itguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.itguigu.gmall.pms.vo.ItemGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao,
        AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroupByPage(QueryCondition condition,
                                   Long catId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();

        if (catId != null) {
            wrapper.eq("catelog_id", catId);
        }
        // 父类的 page 方法，需要两个对象 IPage<T> page, Wrapper<T> queryWrapper
        IPage<AttrGroupEntity> page = this.page(
                //getPage是 Query 中的方法，是将传过来的分页参数condition
                // 转换为page方法需要的 Ipage对象
                new Query<AttrGroupEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public AttrGroupVO queryByGroupId(Long gid) {
        AttrGroupVO attrGroupvo = new AttrGroupVO();

        //根据gid查询改组实体类对象并存入AttGroupvo对象
        AttrGroupEntity attrGroupEntity =
                this.attrGroupDao.selectById(gid);
        BeanUtils.copyProperties(attrGroupEntity, attrGroupvo);
        //根据查到的对象的id集合查询所有中间表的实体类，判断是否为空，若为空则直接返回
        List<AttrAttrgroupRelationEntity> relations =
                this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));

        if (CollectionUtils.isEmpty(relations)) {
            return attrGroupvo;
        }
        attrGroupvo.setRelations(relations);
        //根据查到的中间表对象查询到Attr
        // id集合，根据此id集合查询到所有Attr实体类集合并存入到AttrGroupvo对象中
        List<Long> attrIds =
                relations.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());

        //根据arrtIds查询所有的属性信息
        List<AttrEntity> attrEntities =
                this.attrDao.selectBatchIds(attrIds);

        attrGroupvo.setAttrEntities(attrEntities);

        return attrGroupvo;
    }

    @Override
    public List<AttrGroupVO> queryByCid(Long cid) {
        // 查询所有的分组
        List<AttrGroupEntity> attrGroupEntities =
                this.list(new QueryWrapper<AttrGroupEntity>().eq(
                        "catelog_id", cid));

        // 查询出每组下的规格参数
        List<AttrGroupVO> attrGroupVOS =
                attrGroupEntities.stream().map(attrGroupEntity -> {
            return this.queryByGroupId(attrGroupEntity.getAttrGroupId());
        }).collect(Collectors.toList());

        return attrGroupVOS;
    }

    @Override
    public List<ItemGroupVO> queryItemGroupBySpuIdCatId(Long spuId, Long catalogId) {
        //1、根据catId查询组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catalogId));

        List<ItemGroupVO> ItemGroupVO = attrGroupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVO itemGroupVO = new ItemGroupVO();
            itemGroupVO.setName(attrGroupEntity.getAttrGroupName());

            List<AttrAttrgroupRelationEntity> relationEntities =
            this.attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupEntity.getAttrGroupId()));

            List<Long> relationIds =
            relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrGroupId).collect(Collectors.toList());


            List<ProductAttrValueEntity> productAttrValueEntities =
            this.productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", relationIds));

            itemGroupVO.setBaseAttrs(productAttrValueEntities);

            return itemGroupVO;
        }).collect(Collectors.toList());


        return ItemGroupVO;
    }

}
