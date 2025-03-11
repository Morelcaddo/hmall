package com.heima.search.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "过滤条件实体")
@AllArgsConstructor
public class FiltersDTO {

    @ApiModelProperty("商品分类集合")
    private List<String> category;

    @ApiModelProperty("商品品牌集合")
    private List<String> brand;

}
