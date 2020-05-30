package atguigu.gmallsearch.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseAttrVO implements Serializable {

    private Long productAttributeId;//1
    //当前属性值的所有值
    private List<String> value = new ArrayList<>();
    //属性名称
    private String name;//网络制式，分类
}
