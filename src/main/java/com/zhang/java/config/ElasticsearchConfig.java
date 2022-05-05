package com.zhang.java.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

/**
 * @Date 2022/5/3 20:08
 * @Author zsy
 * @Description elasticsearch配置类
 */
@Configuration
public class ElasticsearchConfig {
    @Value("${elasticSearch.url}")
    private String esUrl;

    @Bean
    RestHighLevelClient restHighLevelClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                //elasticsearch地址
                .connectedTo(esUrl)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

//    @Bean
//    public ElasticsearchRestTemplate elasticsearchRestTemplate(){
//        return new ElasticsearchRestTemplate(restHighLevelClient());
//    }
}
