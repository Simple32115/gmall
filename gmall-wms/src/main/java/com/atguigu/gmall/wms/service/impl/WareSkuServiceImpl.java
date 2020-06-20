package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private StringRedisTemplate RedisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "stock:lock";

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }



    @Override
    @Transactional
    public String checkAndLockStore(List<SkuLockVO> SkuLockVOs) {

        if (CollectionUtils.isEmpty(SkuLockVOs)) {
            return "没有选中的商品";
        }

        //检查并锁定库存
        SkuLockVOs.forEach(SkuLockVO -> {
            lockStore(SkuLockVO);
        });

        //获取因库存不足没被锁定的数据
        List<SkuLockVO> unLockSku = SkuLockVOs.stream().filter(SkuLockVO -> SkuLockVO.getLock() == false).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unLockSku)) {
            //解锁已锁定商品的库存
            List<SkuLockVO> lockSku = SkuLockVOs.stream().filter(SkuLockVO::getLock).collect(Collectors.toList());
            lockSku.forEach(SkuLockVO -> {
                this.wareSkuDao.unLockStore(SkuLockVO.getWareSkuId(), SkuLockVO.getCount());
            });

            //提示锁定失败的商品
            List<Long> skuIds = unLockSku.stream().map(SkuLockVO::getSkuId).collect(Collectors.toList());
            return "下单失败，库存不足" + skuIds.toString();
        }
        String orderToken = SkuLockVOs.get(0).getOrderToken();

        RedisTemplate.opsForValue().set(KEY_PREFIX + orderToken , JSON.toJSONString(SkuLockVOs));

        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","stock.ttl",orderToken);

        return null;
    }

    private void lockStore(SkuLockVO SkuLockVO) {
        RLock lock = this.redissonClient.getLock("stock:" + SkuLockVO.getSkuId());
        lock.lock();
        //查询库存够不够
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkStore(SkuLockVO.getSkuId(), SkuLockVO.getCount());
        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
            //锁定库存信息
            this.wareSkuDao.lockDao(wareSkuEntities.get(0).getId(), SkuLockVO.getCount());
            SkuLockVO.setLock(true);
            SkuLockVO.setWareSkuId(wareSkuEntities.get(0).getId());
        } else {
            SkuLockVO.setLock(false);
        }
        lock.unlock();
    }

}
