package atguigu.gmallsearch.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "goods",type = "info",shards = 3,replicas = 2)
@NoArgsConstructor
@AllArgsConstructor
public class Goods {

    @Id
    private Long skuId;

    @Field(type = FieldType.Keyword,index = false)
    private String pic;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Double)
    private BigDecimal price;
    @Field(type = FieldType.Long)
    private Long sale;
    @Field(type = FieldType.Boolean)
    private Boolean store;//是否有货
    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;
    @Field(type = FieldType.Nested)
    private List<SearchAttr> attrs;
}
