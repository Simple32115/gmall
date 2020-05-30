package com.atguigu.gmall.sms.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguigu.gmall.sms.vo.SkuSaleDTO;
import com.itguigu.gmall.sms.vo.SkuSaleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionDao skuFullReductionDao;

    @Autowired
    private SkuLadderDao skuLadderDao;
    @Autowired
    private SkuBoundsDao skuBoundsDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );
        return new PageVo(page);
    }

    @Override
    public void saveSkuSaleInfo(SkuSaleDTO skuSaleDTO) {
        //3.1 积分优惠
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleDTO, skuBoundsEntity);
        // 数据库保存的是整数0-15，页面绑定是0000-1111
        List<Integer> work = skuSaleDTO.getWork();
        if (!CollectionUtils.isEmpty(work)) {
            skuBoundsEntity.setWork(work.get(0) * 8 + work.get(1) * 4 + work.get(2) * 2 + work.get(3));

            this.save(skuBoundsEntity);
        }
        // 3.2. 满减优惠
        SkuFullReductionEntity skuFullReductionEntity =
                new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleDTO, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleDTO.getFullAddOther());
        this.skuFullReductionDao.insert(skuFullReductionEntity);

        // 3.3. 数量折扣
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleDTO, skuLadderEntity);
        this.skuLadderDao.insert(skuLadderEntity);

    }

    @Override
    public List<SkuSaleVO> querySaleBySkuId(Long skuId) {

        List<SkuSaleVO> skuSaleVOS = new ArrayList<>();
        //查询积分信息
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null) {
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            skuSaleVO.setDesc("积分");

            StringBuffer sb = new StringBuffer();
            if (skuBoundsEntity.getGrowBounds() != null && skuBoundsEntity.getGrowBounds().intValue() > 0) {
                sb.append("成长积分满" + skuBoundsEntity.getGrowBounds());
            }
            if (skuBoundsEntity.getBuyBounds() != null && skuBoundsEntity.getBuyBounds().intValue() > 0) {
                if (sb != null) {
                    sb.append(",");
                }
                sb.append("送" + skuBoundsEntity.getBuyBounds());
            }
            skuSaleVO.setType(sb.toString());
            skuSaleVOS.add(skuSaleVO);
        }
        //查询打折
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null){
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            skuSaleVO.setType("打折");
            skuSaleVO.setDesc("满"+skuLadderEntity.getFullCount()+"件，打"+skuLadderEntity.getDiscount().divide(new BigDecimal(10))+"折");
            skuSaleVOS.add(skuSaleVO);
        }

        //查询满减
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (skuFullReductionEntity != null){
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            skuSaleVO.setDesc("满减");
            skuSaleVO.setType("满"+skuFullReductionEntity.getFullPrice()+"减"+skuFullReductionEntity.getReducePrice());
            skuSaleVOS.add(skuSaleVO);
        }

        return skuSaleVOS;
    }

}
