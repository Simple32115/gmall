package com.atguigu.gmall.order.feign;

import com.itguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClinet extends GmallSmsApi {
}
