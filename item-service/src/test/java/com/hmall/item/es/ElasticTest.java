package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class ElasticTest {
    private RestHighLevelClient client;

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.150.101:9200")
        ));
    }

    @Test
    void testConnect() {
        System.out.println(client);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }


    @Test
    void testAgg() throws IOException {
        //1：创建request对象
        SearchRequest request = new SearchRequest("items");
        //2：配置request参数
        request.source()
                .query(QueryBuilders.matchAllQuery());
        request.source().size(0);
        request.source()
                .aggregation(AggregationBuilders
                        .terms("brand_agg")
                        .field("brand")
                        .size(20)
                        .subAggregation(AggregationBuilders
                                .stats("stats_meric")
                                .field("price")));

        //3：发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

    }


}
