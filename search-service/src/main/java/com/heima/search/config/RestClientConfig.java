package com.heima.search.config;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.heima.search.domain.doc.ItemDoc;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.PageQuery;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.api.client.ItemClient;
import com.hmall.item.domain.dto.ItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RestClientConfig {

    private final RestClientProperties restClientProperties;
    private final ItemClient itemClient;


    @Bean
    public RestHighLevelClient restHighLevelClient() {
        log.info("正在定义RestHighLevelClient,地址：{},索引库名称:{}",
                restClientProperties.getHost(), restClientProperties.getIndexName());
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                HttpHost.create(restClientProperties.getHost())
        ));
        try {
            CreateIndex(restHighLevelClient);
        } catch (IOException e) {
            log.error("创建索引库失败");
        }

        try {
            InitDocument(restHighLevelClient);
        } catch (IOException e) {
            log.error("初始化文档失败");
        }

        return restHighLevelClient;
    }


    private void CreateIndex(RestHighLevelClient client) throws IOException {
        String indexName = restClientProperties.getIndexName();
        boolean exists = client.indices().exists(
                new GetIndexRequest(indexName),
                RequestOptions.DEFAULT);
        if (exists) {
            log.info("索引库已经存在，先删除原有的索引库");
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            client.indices().delete(request, RequestOptions.DEFAULT);
        }
        log.info("正在创建索引库");
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    private void InitDocument(RestHighLevelClient client) throws IOException {
        log.info("正在初始化文档");
        int pageNo = 1;
        int pageSize = 1000;
        PageQuery pageQuery = new PageQuery();
        String indexName = restClientProperties.getIndexName();
        while (true) {
            pageQuery.setPageNo(pageNo);
            pageQuery.setPageSize(pageSize);
            PageDTO<ItemDTO> result = itemClient.queryItemByPage(pageQuery);
            List<ItemDTO> items = result.getList();
            if (CollUtils.isEmpty(items)) {
                log.info("初始化文档完成,共{}页", pageNo);
                return;
            }
            BulkRequest request = new BulkRequest(indexName);
            for (ItemDTO item : items) {
                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                request.add(new IndexRequest()
                        .id(itemDoc.getId())
                        .source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
            }

            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }

    }


    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"stock\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":{\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

}
