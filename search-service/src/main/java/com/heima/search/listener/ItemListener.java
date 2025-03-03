package com.heima.search.listener;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.heima.search.config.RestClientProperties;
import com.heima.search.domain.doc.ItemDoc;
import com.hmall.item.api.client.ItemClient;
import com.hmall.item.domain.dto.ItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemListener {

    private final RestHighLevelClient client;
    private final RestClientProperties restClientProperties;
    private final ItemClient itemClient;

    //新增文档以及全量修改文档的监听方法
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.add.queue", durable = "true",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy")),
            exchange = @Exchange(name = "search.topic"),
            key = "search.item.add"
    ))
    public void addDocument(Long id) throws IOException {
        log.info("监听到新增商品消息");
        ItemDTO item = itemClient.queryItemById(id);
        if (item == null) {
            return;
        }

        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        String doc = JSONUtil.toJsonStr(itemDoc);
        String indexName = restClientProperties.getIndexName();

        IndexRequest request = new IndexRequest(indexName).id(itemDoc.getId());
        request.source(doc, XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    //删除文档的监听方法
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.delete.queue", durable = "true",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy")),
            exchange = @Exchange(name = "search.topic"),
            key = "search.item.delete"
    ))
    public void deleteDocument(Long id) throws IOException {
        log.info("监听到删除商品消息");
        String indexName = restClientProperties.getIndexName();
        DeleteRequest request = new DeleteRequest(indexName, id.toString());
        client.delete(request, RequestOptions.DEFAULT);
    }

}
