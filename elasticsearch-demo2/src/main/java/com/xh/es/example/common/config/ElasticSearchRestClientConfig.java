package com.xh.es.example.common.config;

import lombok.Data;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

/**
 * @author H.Yang
 * @date 2022/11/22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticSearchRestClientConfig extends AbstractElasticsearchConfiguration {

    private String uri;
    private String username;
    private String password;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(uri)
//                .withConnectTimeout(Duration.ofSeconds(5))
//                .withSocketTimeout(Duration.ofSeconds(3))
                .withBasicAuth(username, password)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }


}
