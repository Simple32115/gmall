package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系; InnoDB free: 5120 kB
 * 
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-26 21:35:35
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
