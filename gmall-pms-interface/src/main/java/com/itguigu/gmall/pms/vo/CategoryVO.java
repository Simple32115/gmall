package com.itguigu.gmall.pms.vo;

import com.itguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends CategoryEntity {
   private List<CategoryEntity> subs;
}
