package com.xh.es.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * ES 索引
 *
 * @author H.Yang
 * @date 2022/11/22
 */
@Slf4j
@RunWith(SpringRunner.class)
public class ElasticSearchIndexTest {

    private RestHighLevelClient restClient = null;

    @Before
    public void connection() {
        log.info("创建ES客户端...");
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "elastic"));

        restClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("172.16.1.100", 9200, HttpHost.DEFAULT_SCHEME_NAME)
                ).setHttpClientConfigCallback(f -> f.setDefaultCredentialsProvider(credentialsProvider))
        );


    }

    @After
    public void close() throws IOException {
        if (restClient == null) {
            throw new NullPointerException("ES连接为空");
        }

        log.info("关闭ES客户端...");
        restClient.close();
    }

    /**
     * 创建索引
     *
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("user");
        CreateIndexResponse createIndexResponse = restClient.indices().create(request, RequestOptions.DEFAULT);

        boolean acknowledged = createIndexResponse.isAcknowledged();
        log.info("索引操作：{}", acknowledged);
    }


    /**
     * 查询索引
     *
     * @throws IOException
     */
    @Test
    public void getIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("user");

        GetIndexResponse response = restClient.indices().get(request, RequestOptions.DEFAULT);

        log.info("Aliases：{}", response.getAliases());
        log.info("Mappings：{}", response.getMappings());
        log.info("Settings：{}", response.getSettings());
    }

    /**
     * 删除索引
     *
     * @throws IOException
     */
    @Test
    public void delIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("user");

        AcknowledgedResponse response = restClient.indices().delete(request, RequestOptions.DEFAULT);

        boolean acknowledged = response.isAcknowledged();
        log.info("删除操作：{}", acknowledged);
    }
}