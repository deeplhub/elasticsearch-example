package com.xh.es.service.strategy;

import com.xh.es.common.constant.ElasticSearchConst;
import com.xh.es.common.properties.ElasticSearchProperties;
import com.xh.es.common.util.ElasticSearchUtil;
import com.xh.es.model.dto.PageRequest;
import com.xh.es.model.dto.RequestSearchAfterPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: black tea
 * @date: 2021/9/7 13:22
 */
@Service("searchAfterPageStrategy")
@Slf4j
@RequiredArgsConstructor
public class SearchAfterPageStrategyImpl<T> implements ElasticSearchRequestPageStrategy<T> {

    private final RestHighLevelClient restHighLevelClient;
    private ElasticSearchProperties elasticSearchProperties;

    @Override
    public List<T> list(Class<T> var1, SearchRequest searchRequest, PageRequest pageRequest) throws IOException {
        SearchSourceBuilder searchSourceBuilder = searchRequest.source();
        // 因为pageRequest中的RequestSearchAfterPage包含唯一的标识,所以这里需要做处理
        RequestSearchAfterPage requestSearchAfterPage = (RequestSearchAfterPage) pageRequest;
        String unique = requestSearchAfterPage.getUnique();
        List<SortBuilder<?>> sorts = searchSourceBuilder.sorts();
        if (CollectionUtils.isEmpty(sorts)) {
            sorts = searchSourceBuilder.sort(requestSearchAfterPage.getSort()).sorts();
        } else {
            boolean anyMatchBool = sorts.stream().anyMatch(sortBuilder -> {
                if (sortBuilder instanceof FieldSortBuilder) {
                    FieldSortBuilder f = (FieldSortBuilder) sortBuilder;
                    return f.getFieldName().equals(unique);
                }
                return false;
            });
            if (!anyMatchBool) {
                sorts.add(requestSearchAfterPage.getSort());
            }
        }
        //分页查询
        searchSourceBuilder.size(pageRequest.getLimit());
        Object[] values = requestSearchAfterPage.getValues();
        if (null != values) {
            //存储上一次分页的sort信息
            searchSourceBuilder.searchAfter(values);
        }
        searchRequest.source(searchSourceBuilder);
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), searchRequest);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<T> list = ElasticSearchUtil.searchResponseToList(response, var1, true);
        log.info("size:{},list:{}", list.size(), list);
        return list;
    }

}
