package com.xh.es.service.strategy;

import com.xh.es.common.constant.ElasticSearchConst;
import com.xh.es.common.properties.ElasticSearchProperties;
import com.xh.es.common.util.ElasticSearchUtil;
import com.xh.es.model.dto.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: black tea
 * @date: 2021/9/7 10:54
 */
@Service("fromSizePageStrategy")
@RequiredArgsConstructor
@Slf4j
public class FromSizePageStrategyImpl<T> implements ElasticSearchRequestPageStrategy<T> {

    private final RestHighLevelClient restHighLevelClient;
    private ElasticSearchProperties elasticSearchProperties;

    @Override
    public List<T> list(Class<T> var1, SearchRequest searchRequest, PageRequest pageRequest) throws IOException {
        SearchSourceBuilder searchSourceBuilder = searchRequest.source();
        searchSourceBuilder.from(pageRequest.getPage());
        searchSourceBuilder.size(pageRequest.getLimit());
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), searchRequest);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return ElasticSearchUtil.searchResponseToList(searchResponse, var1);
    }
}
