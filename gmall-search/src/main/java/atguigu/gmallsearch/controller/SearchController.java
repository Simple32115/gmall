package atguigu.gmallsearch.controller;

import atguigu.gmallsearch.pojo.SearchParam;
import atguigu.gmallsearch.pojo.SearchResponseVO;
import atguigu.gmallsearch.service.SearchService;
import com.atguigu.core.bean.Resp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public Resp<Object> search(SearchParam searchParam) throws IOException {
        SearchResponseVO search = this.searchService.search(searchParam);
        return Resp.ok(search);
    }
}
