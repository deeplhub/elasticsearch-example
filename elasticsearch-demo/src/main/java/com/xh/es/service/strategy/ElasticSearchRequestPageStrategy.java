package com.xh.es.service.strategy;

import com.xh.es.model.dto.PageRequest;
import org.elasticsearch.action.search.SearchRequest;

import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: black tea
 * @date: 2021/9/7 10:50
 */
public interface ElasticSearchRequestPageStrategy<T> {

    List<T> list(Class<T> var1, SearchRequest searchRequest, PageRequest pageRequest) throws IOException;
}
