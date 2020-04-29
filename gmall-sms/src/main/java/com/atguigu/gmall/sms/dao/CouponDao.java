package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息; InnoDB free: 5120 kB
 * 
 * @author MrZ
 * @email zww@atguigu.com
 * @date 2020-04-29 11:02:57
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
