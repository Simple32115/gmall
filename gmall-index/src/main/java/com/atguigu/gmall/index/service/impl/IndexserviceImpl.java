package com.atguigu.gmall.index.service.impl;
import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.itguigu.gmall.pms.entity.CategoryEntity;
import com.itguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IndexserviceImpl implements IndexService {
    @Autowired
    private GmallPmsClient gmallPmsApi;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates";

    @Override
    public List<CategoryEntity> queryLv1Categories() {
        Resp<List<CategoryEntity>> listResp = this.gmallPmsApi.queryCategoriesByPidOrLevel(1, null);
        List<CategoryEntity> data = listResp.getData();
        return data;
    }

    @Override
    public List<CategoryVO> querySubByPid(Long pid) {
        //1. 判断缓存中有没有
        String cateJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        //2. 有，直接返回
        if (!StringUtils.isEmpty(cateJson)) {
            //反序列化成集合后返回
            return JSON.parseArray(cateJson,CategoryVO.class);
        }
        RLock lock = this.redissonClient.getLock("lock" + pid);
        lock.lock();

        String cateJson1 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        //2. 有，直接返回
        if (!StringUtils.isEmpty(cateJson)) {
            //反序列化成集合后返回
            lock.unlock();
            return JSON.parseArray(cateJson,CategoryVO.class);
        }


        //查询数据库
        Resp<List<CategoryVO>> listResp = this.gmallPmsApi.querySubCategoryByPid(pid);
        List<CategoryVO> categoryVOS = listResp.getData();

        //查询完成之后放入缓存
        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVOS), 7 + new Random().nextInt(10), TimeUnit.DAYS);
        lock.unlock();

        return categoryVOS;
    }

    @Override
    public  void lockstest() {

        RLock lock = this.redissonClient.getLock("cock"); //只要锁的名称相同就是同一把锁
        lock.lock();            //加锁
        // 查询redis中的num值
        String value = this.redisTemplate.opsForValue().get("num");
        //没有该值return
        if (StringUtils.isEmpty(value)){
            return;
        }
        //有值就转成Int
        int num = Integer.parseInt(value);
        this.redisTemplate.opsForValue().set("num",String.valueOf(++num));

        lock.unlock();
    }
}
