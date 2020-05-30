package com.atguigu.gmall.index.service;

import com.itguigu.gmall.pms.entity.CategoryEntity;
import com.itguigu.gmall.pms.vo.CategoryVO;

import java.util.List;


public interface IndexService {
    List<CategoryEntity> queryLv1Categories();

    List<CategoryVO> querySubByPid(Long pid);

    void lockstest();
}
