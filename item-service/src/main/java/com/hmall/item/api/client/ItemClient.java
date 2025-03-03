package com.hmall.item.api.client;


import com.hmall.common.config.DefaultFeignConfig;
import com.hmall.common.domain.OrderDetailDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.PageQuery;
import com.hmall.item.api.config.ItemFeignConfig;
import com.hmall.item.api.fallback.ItemClientFallback;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.po.Item;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service", fallbackFactory = ItemClientFallback.class,
        configuration = {DefaultFeignConfig.class, ItemFeignConfig.class})
public interface ItemClient {
    @GetMapping("/items")
    List<ItemDTO> queryItemsByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    @GetMapping("/items/{id}")
    ItemDTO queryItemById(@PathVariable("id") Long id);

    @GetMapping("/items/page")
    PageDTO<ItemDTO> queryItemByPage(@SpringQueryMap PageQuery query);

}
