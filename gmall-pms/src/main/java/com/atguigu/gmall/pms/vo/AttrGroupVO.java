package com.atguigu.gmall.pms.vo;

import com.itguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.itguigu.gmall.pms.entity.AttrEntity;
import com.itguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupVO extends AttrGroupEntity {
    private List<AttrEntity> attrEntities;
    private List<AttrAttrgroupRelationEntity> relations;

}
