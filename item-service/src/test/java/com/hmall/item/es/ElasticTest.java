package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
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
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctions
                = {new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                QueryBuilders.termQuery("isAD", true),
                ScoreFunctionBuilders.weightFactorFunction(1000))
        };


        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(),
                        filterFunctions).boostMode(
                        CombineFunction.REPLACE);

        request.source().query(functionScoreQuery);

        //3：发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

    }


}
