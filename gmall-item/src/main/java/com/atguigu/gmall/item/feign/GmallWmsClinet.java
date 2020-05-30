package com.atguigu.gmall.item.feign;

import com.itguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsClinet extends GmallWmsApi {
}
