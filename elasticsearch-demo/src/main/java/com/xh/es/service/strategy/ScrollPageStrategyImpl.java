package com.xh.es.service.strategy;

import com.xh.es.common.constant.ElasticSearchConst;
import com.xh.es.common.properties.ElasticSearchProperties;
import com.xh.es.common.util.ElasticSearchUtil;
import com.xh.es.model.dto.PageRequest;
import com.xh.es.model.dto.RequestScrollPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service("scrollPageStrategy")
@RequiredArgsConstructor
@Slf4j
public class ScrollPageStrategyImpl<T> implements ElasticSearchRequestPageStrategy<T> {

    private final RestHighLevelClient restHighLevelClient;
    private ElasticSearchProperties elasticSearchProperties;

    @Override
    public List<T> list(Class<T> var1, SearchRequest searchRequest, PageRequest pageRequest) throws IOException {
        List<T> resultList = new ArrayList<>();
        RequestScrollPage requestScrollPage = (RequestScrollPage) pageRequest;
        //失效时间
        TimeValue scrollTimeValue = requestScrollPage.getScrollTimeValue();
        if (null == scrollTimeValue) {
            throw new RuntimeException("ES使用SCROLL分页,必须要传入失效时间!");
        }
        Scroll scroll = new Scroll(scrollTimeValue);
        //封存快照
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = searchRequest.source();
        searchSourceBuilder.size(pageRequest.getLimit());
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), searchRequest);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId;
        do {
            resultList.addAll(ElasticSearchUtil.searchResponseToList(searchResponse, var1));
            //每次循环完后取得scrollId,用于记录下次将从这个游标开始取数
            scrollId = searchResponse.getScrollId();
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), scrollRequest);
            //进行下次查询
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
        } while (searchResponse.getHits().getHits().length != 0);
        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        //也可以选择setScrollIds()将多个scrollId一起使用
        clearScrollRequest.addScrollId(scrollId);
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), clearScrollRequest);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        if (!clearScrollResponse.isSucceeded()) {
            log.error("scroll清除失败,scrollId={}", scrollId);
        }
        return resultList;
    }

}
