package com.xh.es.service.impl;


import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.xh.es.common.annotation.ExecutionMethod;
import com.xh.es.common.constant.ElasticSearchConst;
import com.xh.es.common.properties.ElasticSearchProperties;
import com.xh.es.common.util.ElasticSearchUtil;
import com.xh.es.model.dto.*;
import com.xh.es.service.ElasticSearchService;
import com.xh.es.service.strategy.ElasticSearchRequestPageStrategy;
import com.xh.es.service.strategy.RequestPageContext;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.BulkByScrollTask;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ?????? RestHighLevelClient ?????????ElasticsearchService ??????
 *
 * @author H.Yang
 * @date 2022/11/22
 */
@Slf4j
@Service
public class ElasticSearchServiceImpl<T> implements ElasticSearchService<T> {

    @Resource(name = "fromSizePageStrategy")
    private ElasticSearchRequestPageStrategy<T> fromSizePageStrategy;
    @Resource(name = "scrollPageStrategy")
    private ElasticSearchRequestPageStrategy<T> scrollPageStrategy;
    @Resource(name = "searchAfterPageStrategy")
    private ElasticSearchRequestPageStrategy<T> searchAfterPageStrategy;

    private RestHighLevelClient restHighLevelClient;
    private ElasticSearchProperties elasticSearchProperties;

    public ElasticSearchServiceImpl(RestHighLevelClient restHighLevelClient, ElasticSearchProperties elasticSearchProperties) {
        this.restHighLevelClient = restHighLevelClient;
        this.elasticSearchProperties = elasticSearchProperties;
    }


    @Override
    @ExecutionMethod(name = "es-????????????(??????)", des = "??????Object var2,String id?????????boolean")
    public boolean createDocument(Object var2, String index, String id) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index);
        indexRequest.id(id);
        indexRequest.source(JSONUtil.toJsonStr(var2), XContentType.JSON);

        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), indexRequest);

        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        RestStatus status = indexResponse.status();
        return RestStatus.CREATED.equals(status);
    }

    @Override
    @ExecutionMethod(name = "es-????????????(??????)", des = "??????Object var2,String id?????????T")
    public T createDocument(Class<T> var1, Object var2, String index, String id) throws IOException {
        if (this.createDocument(var2, index, id)) {
            return this.getDocument(var1, index, id);
        }
        return null;
    }

    @Override
    @ExecutionMethod(name = "es-????????????(??????)", des = "??????Map<String, Object> var,??????boolean")
    public boolean addBatchDocument(Map<String, Object> var, String index) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        var.forEach((k, v) -> {
            IndexRequest indexRequest = new IndexRequest(index);
            indexRequest.id(k);
            indexRequest.source(JSONUtil.toJsonStr(v), XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        RestStatus status = bulkResponse.status();
        return RestStatus.OK.equals(status);
    }

    @Override
    @ExecutionMethod(name = "es-??????id??????(??????)", des = "??????String id,??????boolean")
    public boolean deleteDocument(String index, String id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(index, id);
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), deleteRequest);
        DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        RestStatus status = delete.status();
        return RestStatus.OK.equals(status);
    }

    @Override
    @ExecutionMethod(name = "es-?????? ids?????? ??????????????????")
    public boolean deleteBatchDocument(String index, List<String> ids) throws IOException {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RuntimeException("ES??????_id???????????????????????????,ids??????????????????size???0!");
        }
        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(id -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, id);
            bulkRequest.add(deleteRequest);
        });
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        RestStatus status = bulkResponse.status();
        return RestStatus.OK.equals(status);
    }

    @Override
    @ExecutionMethod(name = "es-??????key??????(??????)", des = "??????String key, Object value,??????boolean")
    public long deleteDocument(String index, String key, Object value) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index);
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder(key, value);
        deleteByQueryRequest.setQuery(termQueryBuilder);
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), deleteByQueryRequest);
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        BulkByScrollTask.Status status = bulkByScrollResponse.getStatus();
        return status.getDeleted();
    }

    @Override
    @ExecutionMethod(name = "es-?????????????????????????????????,?????????????????????")
    public long deleteDocumentByCondition(String index, List<ElasticSearchConditionDTO> conditionDTOS) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index);
        // ???????????????????????????
        SearchSourceBuilder searchSourceBuilder = ElasticSearchUtil.conditionCombination(new ElasticSearchSearchDTO(conditionDTOS));
        deleteByQueryRequest.setQuery(searchSourceBuilder.query());
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), deleteByQueryRequest);
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        return bulkByScrollResponse.getDeleted();
    }

    @Override
    @ExecutionMethod(name = "es-??????id??????(??????)", des = "?????? Object???id,??????boolean")
    public boolean updateDocument(Object var2, String index, String id) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(index, id);
        updateRequest.doc(JSONUtil.toJsonStr(var2), XContentType.JSON);
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), updateRequest);
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        RestStatus status = update.status();
        return RestStatus.OK.equals(status);
    }

    @Override
    @ExecutionMethod(name = "es-??????id??????(??????)", des = "?????? Object???id,??????T")
    public T updateDocument(Class<T> var1, Object var2, String index, String id) throws IOException {
        if (this.updateDocument(var2, index, id)) {
            return this.getDocument(var1, index, id);
        }
        return null;
    }

    @Override
    @ExecutionMethod(name = "es-?????????????????????????????????,?????????????????????")
    public long updateDocumentByCondition(Object var2, String index, List<ElasticSearchConditionDTO> conditionDTOS) throws IOException {
        UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(index);
        // ???????????????????????????
        SearchSourceBuilder searchSourceBuilder = ElasticSearchUtil.conditionCombination(new ElasticSearchSearchDTO(conditionDTOS));
        updateByQueryRequest.setQuery(searchSourceBuilder.query());
        // ??????code
        ScriptDTO scriptDto = ElasticSearchUtil.scriptCombination(var2);
        updateByQueryRequest.setScript(new Script(ScriptType.INLINE,
                scriptDto.getLang(),
                scriptDto.getScript(), scriptDto.getParams()));
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), updateByQueryRequest);
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
        return bulkByScrollResponse.getUpdated();
    }

    @Override
    @ExecutionMethod(name = "es-?????? ScriptDto ??????????????????,?????????????????????")
    public long updateDocumentByCondition(ScriptDTO scriptDto, String index, List<ElasticSearchConditionDTO> conditionDTOS) throws IOException {
        UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(index);
        // ???????????????????????????
        SearchSourceBuilder searchSourceBuilder = ElasticSearchUtil.conditionCombination(new ElasticSearchSearchDTO(conditionDTOS));
        updateByQueryRequest.setQuery(searchSourceBuilder.query());
        updateByQueryRequest.setScript(ElasticSearchUtil.toScript(scriptDto));
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), updateByQueryRequest);
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
        return bulkByScrollResponse.getUpdated();
    }

    @Override
    @ExecutionMethod(name = "es-??????Map -> k?????????id????????????????????????")
    public boolean updateBatchDocument(String index, Map<String, Object> params) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        if (MapUtil.isEmpty(params)) {
            throw new RuntimeException("ES?????????????????????,??????Map??????????????????size???0!");
        }
        params.forEach((k, v) -> {
            UpdateRequest updateRequest = new UpdateRequest(index, k);
            updateRequest.doc(JSONUtil.toJsonStr(v), XContentType.JSON);
            bulkRequest.add(updateRequest);
        });
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        RestStatus status = bulkResponse.status();
        return RestStatus.OK.equals(status);
    }

    @Override
    @ExecutionMethod(name = "es-??????id??????(??????)", des = "?????? id")
    public T getDocument(Class<T> var1, String index, String id) throws IOException {
        GetRequest getRequest = new GetRequest(index, id);
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), getRequest);
        GetResponse documentFields = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        return JSONUtil.toBean(documentFields.getSourceAsString(), var1);
    }

    @Override
    @ExecutionMethod(name = "es-?????? map ??????????????????")
    public List<T> getListByAndMap(Class<T> var1, String index, Map<String, Object> map) throws IOException {
        return this.getListByAndMap(var1, index, map, null);
    }

    @Override
    @ExecutionMethod(name = "es-??????Map??????and-tery??????(list)", des = "?????? Map<String, Object>???Map<String, SortOrder>??????")
    public List<T> getListByAndMap(Class<T> var1, String index, Map<String, Object> map, Map<String, SortOrder> sortOrderMap) throws IOException {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (MapUtil.isNotEmpty(map)) {
            map.forEach((k, v) -> boolQueryBuilder.must(QueryBuilders.termQuery(k, v)));
        }
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(boolQueryBuilder);
        if (MapUtil.isNotEmpty(sortOrderMap)) {
            sortOrderMap.forEach(searchSourceBuilder::sort);
        }
        searchRequest.source(searchSourceBuilder);
        ElasticSearchUtil.esLog(ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel()), searchRequest);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return ElasticSearchUtil.searchResponseToList(searchResponse, var1);
    }

    @Override
    @ExecutionMethod(name = "es-??????????????????(list)", des = "?????? List<ESConditionDTO>??????")
    public List<T> getListByCondition(Class<T> var1, String index, List<ElasticSearchConditionDTO> conditionDos) throws IOException {
        return this.getListByCondition(var1, index, new ElasticSearchSearchDTO(conditionDos));
    }

    @Override
    @ExecutionMethod(name = "es-??????????????????(list)", des = "?????? ESSearchDto??????,from-size/scroll,count??????10000???scroll")
    public List<T> getListByCondition(Class<T> var1, String index, ElasticSearchSearchDTO searchDto) throws IOException {
         /*
           ?????????????????????????????????,????????????????????????!
           1: ?????????????????????????
             ??????: es ?????????????????? 0???10 ??????,???????????????????????????10????????????
           2: ????????????(???????????????????????????)?
             ????????????????????????????????????,?????????????????????????????????????
             ?????????(count) <= 0 ??????????????? new ArrayList<T>
             ?????????count <= 10000 ????????? from-size ?????? {@link com.blacktea.es.service.strategy.FromSizePageStrategyImpl}
             ?????????(count) > 10000 ???,???????????? scroll ????????? ??? search_after ?????????,
               ???????????????????????? scroll ?????? {@link com.blacktea.es.service.strategy.ScrollPageStrategyImpl}
         */

        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = ElasticSearchUtil.conditionCombination(searchDto);
        searchRequest.source(searchSourceBuilder);
        QueryBuilder query = searchSourceBuilder.query();
        long count = this.count(index, query);
        int limit;
        if (count <= ElasticSearchConst.ZERO) {
            return new ArrayList<>();
        } else {
            if (count <= ElasticSearchConst.MAX_FROMSIZE_COUNT) {
                RequestFromSizePage requestFromSizePage = new RequestFromSizePage();
                requestFromSizePage.setPage(0);
                limit = Math.toIntExact(count);
                requestFromSizePage.setLimit(limit);
                List<T> list = this.list(var1, requestFromSizePage, searchRequest);
                return list;
            } else {
                // ???????????????????????????
                RequestScrollPage requestScrollPage = new RequestScrollPage();
                requestScrollPage.setPage(0);
                limit = ElasticSearchConst.SCROLL_LIMIT;
                requestScrollPage.setLimit(limit);
                requestScrollPage.setScrollTimeValue(TimeValue.timeValueMinutes(2));
                List<T> list = this.list(var1, requestScrollPage, searchRequest);
                return list;
            }
        }
    }

    /**
     * ??????list, ???????????? {@link com.xh.es.service.strategy.ElasticSearchRequestPageStrategy} ????????????????????????
     *
     * @param var1          ???????????????
     * @param pageRequest   ??????????????????
     * @param searchRequest ??????????????????
     * @return List<T>
     * @throws IOException ??????
     */
    private List<T> list(Class<T> var1, PageRequest pageRequest, SearchRequest searchRequest) throws IOException {
        List<T> list = new ArrayList<>();
        if (null == pageRequest) {
            throw new RuntimeException("ES???????????????,???????????????????????????!");
        }

        if (pageRequest instanceof RequestFromSizePage) {
            // ??????????????????
            list = new RequestPageContext<>(fromSizePageStrategy).list(var1, searchRequest, pageRequest);
        } else if (pageRequest instanceof RequestScrollPage) {
            // ??????????????????
            list = new RequestPageContext<>(scrollPageStrategy).list(var1, searchRequest, pageRequest);
        } else if (pageRequest instanceof RequestSearchAfterPage) {
            // ??????????????????
            list = new RequestPageContext<>(searchAfterPageStrategy).list(var1, searchRequest, pageRequest);
        }
        return list;

    }

    @Override
    @ExecutionMethod(name = "es-????????????????????????(page)")
    public Page<T> getPageByCondition(Class<T> var1, String index, ElasticSearchSearchDTO esSearchDto, PageRequest pageRequest) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = ElasticSearchUtil.conditionCombination(esSearchDto);
        searchRequest.source(searchSourceBuilder);
        List<T> list = this.list(var1, pageRequest, searchRequest);
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
        }
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getLimit());
        SearchSourceBuilder source = searchRequest.source();
        long count = this.count(index, source.query());
        return new PageImpl<>(list, pageable, count);
    }

    @Override
    @ExecutionMethod(name = "es-????????????????????????(from-size)")
    public Page<T> getPageFromSizeByCondition(Class<T> var1, String index, ElasticSearchSearchDTO esSearchDto, RequestFromSizePage requestFromSizePage) throws IOException {
        return this.getPageByCondition(var1, index, esSearchDto, requestFromSizePage);
    }

    @Override
    @ExecutionMethod(name = "es-??????????????????(count)")
    public Long count(String index, QueryBuilder query) throws IOException {
        ElasticSearchConst.ESLogLevelEnum byLevel = ElasticSearchConst.ESLogLevelEnum.getByLevel(elasticSearchProperties.getLevel());
        CountRequest countRequest = new CountRequest(index);
        countRequest.query(query);
        ElasticSearchUtil.esLog(byLevel, countRequest);
        CountResponse countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        long count = countResponse.getCount();
        log.info("count -> ???????????????????????????{}???!", count);
        return count;
    }


}
