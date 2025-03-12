package com.heima.search.controller;


import com.alibaba.fastjson.JSONObject;
import com.heima.search.config.RestClientProperties;
import com.heima.search.domain.dto.FiltersDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.query.ItemPageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final RestHighLevelClient client;
    private final RestClientProperties properties;

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) throws IOException {
        SearchRequest request = new SearchRequest(properties.getIndexName());
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String key = query.getKey();
        String brand = query.getBrand();
        String category = query.getCategory();
        Integer minPrice = query.getMinPrice();
        Integer maxPrice = query.getMaxPrice();
        Integer pageNo = query.getPageNo();
        Integer pageSize = query.getPageSize();
        Boolean isAsc = query.getIsAsc();
        String sortBy = query.getSortBy();
        if (key != null && !key.isEmpty()) {
            boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if (brand != null && !brand.isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if (category != null && !category.isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        if (minPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }
        if (maxPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }


        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctions
                = {new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                QueryBuilders.termQuery("isAD", true),
                ScoreFunctionBuilders.weightFactorFunction(1000))
        };


        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(boolQuery,
                        filterFunctions).boostMode(
                        CombineFunction.REPLACE);

        request.source().query(functionScoreQuery);


//        request.source().query(boolQuery);


//        if (sortBy == null || sortBy.isEmpty()) {
//            sortBy = "updateTime";
//        }
//        if (isAsc == null) {
//            isAsc = true;
//        }

        if(sortBy != null && !sortBy.isEmpty()){
            if (isAsc) {
                request.source().sort(sortBy, SortOrder.ASC);
            } else {
                request.source().sort(sortBy, SortOrder.DESC);
            }
            request.source().sort("_score", SortOrder.DESC);
        }


        request.source().from((pageNo - 1) * pageSize).size(pageSize);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        SearchHits searchHits = response.getHits();

        Long total = searchHits.getTotalHits().value;
        Long pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;

        log.info("共搜索到" + total + "条数据，" + "总页数" + pages);
        SearchHit[] hits = searchHits.getHits();
        List<ItemDTO> result = new ArrayList<>();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            JSONObject parse = JSONObject.parseObject(json);
            ItemDTO itemDTO = parse.toJavaObject(ItemDTO.class);
            result.add(itemDTO);
        }

        return new PageDTO<>(total, pages, result);

    }

    @ApiOperation("搜索过滤条件")
    @PostMapping("/filters")
    public FiltersDTO filters(@RequestBody ItemPageQuery query) throws IOException {
        SearchRequest request = new SearchRequest(properties.getIndexName());

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        String key = query.getKey();
        String brand = query.getBrand();
        String category = query.getCategory();
        Integer minPrice = query.getMinPrice();
        Integer maxPrice = query.getMaxPrice();

        log.info("商品的品牌信息：{}", brand);
        log.info("商品的分类信息：{}", category);
        log.info("商品的关键字段：{}", key);
        if (key != null && !key.isEmpty()) {
            boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if (brand != null && !brand.isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if (category != null && !category.isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        if (minPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }
        if (maxPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }

        request.source().query(boolQuery);
//        request.source().size(0);

        request.source().aggregation(AggregationBuilders.terms("brand_agg")
                .field("brand"));

        request.source().aggregation(AggregationBuilders.terms("category_agg")
                .field("category"));

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms brandTerms = aggregations.get("brand_agg");
        Terms categoryTerms = aggregations.get("category_agg");

        List<String> brands = new ArrayList<>();
        List<String> categories = new ArrayList<>();


        List<? extends Terms.Bucket> BrandBuckets = brandTerms.getBuckets();

        BrandBuckets.forEach((Consumer<Terms.Bucket>) bucket ->
                brands.add(bucket.getKeyAsString()));

        List<? extends Terms.Bucket> categoryBuckets = categoryTerms.getBuckets();
        categoryBuckets.forEach((Consumer<Terms.Bucket>) bucket ->
                categories.add(bucket.getKeyAsString()));

        return new FiltersDTO(categories, brands);
    }

}
