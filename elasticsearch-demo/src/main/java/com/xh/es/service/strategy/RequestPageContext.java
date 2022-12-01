package com.xh.es.service.strategy;

import com.xh.es.model.dto.PageRequest;
import org.elasticsearch.action.search.SearchRequest;

import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: black tea
 * @date: 2021/9/7 13:28
 */
public class RequestPageContext<T> {

    private ElasticSearchRequestPageStrategy<T> elasticSearchRequestPageStrategy;

    private RequestPageContext(){

    }

    public RequestPageContext(ElasticSearchRequestPageStrategy<T> elasticSearchRequestPageStrategy) {
        this.elasticSearchRequestPageStrategy = elasticSearchRequestPageStrategy;
    }

    public List<T> list(Class<T> var1, SearchRequest searchRequest, PageRequest pageRequest) throws IOException {
        List<T> list = elasticSearchRequestPageStrategy.list(var1,searchRequest,pageRequest);
        return list;
    }
}
