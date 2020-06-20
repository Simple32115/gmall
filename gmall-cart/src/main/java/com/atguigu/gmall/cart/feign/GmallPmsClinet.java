package com.atguigu.gmall.cart.feign;

import com.itguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface GmallPmsClinet extends GmallPmsApi {
}
