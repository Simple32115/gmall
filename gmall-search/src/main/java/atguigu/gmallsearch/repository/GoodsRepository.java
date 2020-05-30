package atguigu.gmallsearch.repository;

import atguigu.gmallsearch.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
    
}
