package com.xh.es.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author H.Yang
 * @date 2022/11/22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticSearchProperties {

    private String uri;
    private String username;
    private String password;
    private int level;
}
