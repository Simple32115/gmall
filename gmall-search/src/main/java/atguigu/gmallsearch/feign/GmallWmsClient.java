package atguigu.gmallsearch.feign;


import com.atguigu.gmall.wms.vo.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
