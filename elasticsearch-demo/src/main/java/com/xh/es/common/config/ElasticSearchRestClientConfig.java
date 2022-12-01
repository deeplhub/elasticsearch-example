package com.xh.es.common.config;

import com.xh.es.common.properties.ElasticSearchProperties;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

/**
 * @author H.Yang
 * @date 2022/11/22
 */
@Configuration
public class ElasticSearchRestClientConfig extends AbstractElasticsearchConfiguration {

    private ElasticSearchProperties elasticSearchProperties;

    public ElasticSearchRestClientConfig(ElasticSearchProperties elasticSearchProperties) {
        this.elasticSearchProperties = elasticSearchProperties;
    }

    @Bean
    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticSearchProperties.getUri())
//                .withConnectTimeout(Duration.ofSeconds(5))
//                .withSocketTimeout(Duration.ofSeconds(3))
                .withBasicAuth(elasticSearchProperties.getUsername(), elasticSearchProperties.getPassword())
                .build();
        return RestClients.create(clientConfiguration).rest();
    }


}
