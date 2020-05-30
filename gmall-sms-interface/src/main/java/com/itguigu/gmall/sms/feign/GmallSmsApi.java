package com.itguigu.gmall.sms.feign;

import com.atguigu.core.bean.Resp;
import com.itguigu.gmall.sms.vo.SkuSaleDTO;
import com.itguigu.gmall.sms.vo.SkuSaleVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

public interface GmallSmsApi {

    @RequestMapping("sms/skubounds/{skuId}")
    public Resp<List<SkuSaleVO>> querySaleBySkuId(@PathVariable("skuId")Long skuId);

    @PostMapping("sms/spubounds/skusale/save")
    public Resp<Object> saveSkuSaleInfo(@RequestBody SkuSaleDTO skuSaleDTO);
}
