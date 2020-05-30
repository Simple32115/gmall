package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.itguigu.gmall.pms.entity.CategoryEntity;
import com.itguigu.gmall.pms.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {
    @Autowired
    private IndexService indexService;

    @RequestMapping("cates")
    public Resp<List<CategoryEntity>> queryLv1Catgories(){
        List<CategoryEntity> categoryEntities =this.indexService.queryLv1Categories();

        return Resp.ok(categoryEntities);
    }

    @RequestMapping("cates/{pid}")
    public Resp<List<CategoryVO>> querySubByPid(@PathVariable("pid")Long pid){
        List<CategoryVO> categoryVO = this.indexService.querySubByPid(pid);
        return Resp.ok(categoryVO);

    }
    @GetMapping("test")
    public Resp<Object> lockstest(){
        this.indexService.lockstest();

        return Resp.ok(null);
    }
}
