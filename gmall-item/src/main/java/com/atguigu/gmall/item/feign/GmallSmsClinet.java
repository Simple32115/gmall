package com.atguigu.gmall.item.feign;

import com.itguigu.gmall.sms.feign.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClinet extends GmallSmsApi {
}
