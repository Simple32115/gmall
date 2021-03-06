package com.atguigu.gmall.item.controller;


import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @RequestMapping("{skuId}")
    public Resp<ItemVO> queryItemBySkuId(@PathVariable Long skuId){
        ItemVO itemVO = this.itemService.queryItemBySkuId(skuId);

        return Resp.ok(itemVO);
    }
}
