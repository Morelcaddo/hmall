package com.heima.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hm.search.elasticsearch")
public class RestClientProperties {
    private String host;
    private String indexName;
}
