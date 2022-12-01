package com.xh.es.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xh.es.common.annotation.ExecutionMethod;
import com.xh.es.common.constant.ElasticSearchConst;
import com.xh.es.model.dto.ElasticSearchConditionDTO;
import com.xh.es.model.dto.ElasticSearchSearchDTO;
import com.xh.es.model.dto.ScriptDTO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 * @author H.Yang
 * @date 2022/11/23
 */
@Slf4j
public class ElasticSearchUtil {

    /**
     * 拼接查询条件
     *
     * @param esSearchDto 条件
     * @return SearchSourceBuilder
     */
    public static SearchSourceBuilder conditionCombination(ElasticSearchSearchDTO esSearchDto) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<QueryBuilder> mustClauses = new ArrayList<>();
        List<QueryBuilder> shouldClauses = new ArrayList<>();
        List<ElasticSearchConditionDTO> conditionDtos = esSearchDto.getConditionDTOS();
        if (!CollectionUtils.isEmpty(conditionDtos)) {
            conditionDtos.forEach(c -> {
                // 拼接查询内容
                QueryBuilder queryOperation = ElasticSearchUtil.getQueryOperation(c);
                if (c.isAnd()) {
                    mustClauses.add(queryOperation);
                } else {
                    shouldClauses.add(queryOperation);
                }
            });
        }
        // 拼接排序
        Map<String, SortOrder> sortOrderMap = esSearchDto.getSortOrderMap();
        if (MapUtil.isNotEmpty(sortOrderMap)) {
            sortOrderMap.forEach(searchSourceBuilder::sort);
        }
        if (!CollectionUtils.isEmpty(mustClauses)) {
            mustClauses.forEach(boolQueryBuilder::must);
        }
        if (!CollectionUtils.isEmpty(shouldClauses)) {
            shouldClauses.forEach(boolQueryBuilder::should);
        }
        return searchSourceBuilder.query(boolQueryBuilder);
    }

    /**
     * 拼接查询方式,例如 term、match等
     *
     * @param dto 条件
     * @return QueryBuilder
     */
    public static QueryBuilder getQueryOperation(ElasticSearchConditionDTO dto) {
        switch (dto.getOperation()) {
            case TERM_QUERY:
                return QueryBuilders.termQuery(dto.getK(), dto.getV());
            case MULTI_MATCH_QUERY:
                return QueryBuilders.matchQuery(dto.getK(), dto.getV());
            default:
                log.error("请配置该操作类型的case!");
                throw new RuntimeException("组合条件时,发现缺少对应的case逻辑!");
        }
    }

    /**
     * 根据实体类自动转换为ScriptDto {@link ScriptDTO}
     * ScriptDto.params = 每个不为空的属性的名称和值组成 -> map的k,v
     * ScriptDto.script = 每个不为空的属性的名称和值组成,且该条件均为覆盖原值(重新该字段赋值)
     *
     * @param var2 修改后的内容
     * @return ScriptDto
     */
    public static ScriptDTO scriptCombination(Object var2) {
        JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(var2));
        Map<String, Object> map = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        jsonObject.entrySet().forEach(o -> {
            builder.append("ctx._source.");
            builder.append(o.getKey());
            builder.append("=params.");
            builder.append(o.getKey());
            builder.append(";");
            map.put(o.getKey(), o.getValue());
        });
        return new ScriptDTO(ScriptType.INLINE, "painless", builder.toString(), map);
    }

    /**
     * 用于检查 ScriptDto 是否合理！
     *
     * @param scriptDto
     */
    public static void checkScript(ScriptDTO scriptDto) {
        if (null == scriptDto
                || !StringUtils.hasLength(scriptDto.getLang())
                || !StringUtils.hasLength(scriptDto.getScript())
                || MapUtil.isNotEmpty(scriptDto.getParams())
        ) {
            log.error("SCRIPT组合错误,当前ScriptDto对象:{}", scriptDto);
        }
    }

    /**
     * 用于将 ScriptDto 转化为 Script
     *
     * @param scriptDto 自定义的ScriptDto
     * @return Script
     */
    public static Script toScript(ScriptDTO scriptDto) {
        checkScript(scriptDto);
        return new Script(scriptDto.getType(),
                scriptDto.getLang(),
                scriptDto.getScript(), scriptDto.getParams());
    }

    /**
     * 查询结果使用 fastjson 转换为 List<T>
     * 用于非{@link com.xh.es.service.strategy.SearchAfterPageStrategyImpl} 场景
     *
     * @param searchResponse es查询结果,从中取出 Hits 进行转换
     * @param var1           返回的对象类型
     * @param <T>            返回的对象类型
     * @return List<T>
     */
    public static <T> List<T> searchResponseToList(SearchResponse searchResponse, Class<T> var1) {
        return searchResponseToList(searchResponse, var1, false);
    }

    /**
     * 查询结果使用 fastjson 转换为 List<T>
     *
     * @param searchResponse es查询结果,从中取出 Hits 进行转换
     * @param var1           返回的对象类型
     * @param <T>            返回的对象类型
     * @param source         是否需要用于 {@link com.xh.es.service.strategy.SearchAfterPageStrategyImpl} 场景
     * @return List<T>
     */
    public static <T> List<T> searchResponseToList(SearchResponse searchResponse, Class<T> var1, boolean source) {
        StringBuilder arrayJson = new StringBuilder("[");
        StringJoiner joiner = new StringJoiner(",");
        Arrays.stream(searchResponse.getHits().getHits()).forEach(s -> {
            if (source) {
                JSONObject jsonObject = JSONUtil.parseObj(s.getSourceAsString());
                jsonObject.putOpt("sort", s.getSortValues());
                joiner.add(JSONUtil.toJsonStr(jsonObject));
            } else {
                joiner.add(s.getSourceAsString());
            }
        });
        arrayJson.append(joiner.toString());
        arrayJson.append("]");
        return JSONUtil.toList(arrayJson.toString(), var1);
    }


    public static void esOperationLog(ElasticSearchConst.ESLogLevelEnum esLogLevelEnum, ExecutionMethod executionMethod, String prefix) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("status", prefix);
        String name = executionMethod.name();
        jsonObject.putOpt("name", name);
        jsonObject.putOpt("des", executionMethod.des());
        jsonObject.putOpt("date", DateUtil.now());
        String content = prefix + "执行[" + name + "]方法";
        switch (esLogLevelEnum) {
            case DEBUG:
                log.debug("{},具体描述:{}", content, JSONUtil.toJsonStr(jsonObject));
                break;
            case INFO:
                log.info("{},具体描述:{}", content, JSONUtil.toJsonStr(jsonObject));
                break;
            case ERROR:
                log.error("{},具体描述:{}", content, JSONUtil.toJsonStr(jsonObject));
                break;
            default:
        }
    }

    /**
     * 记录 ES DSL 日志,便于排错
     *
     * @param esLogLevelEnum 级别
     * @param request        请求对象
     */
    public static void esLog(ElasticSearchConst.ESLogLevelEnum esLogLevelEnum, ActionRequest request) {
        if (request instanceof SearchRequest) {
            SearchRequest searchRequest = (SearchRequest) request;
            SearchSourceBuilder searchSourceBuilder = searchRequest.source();
            esLog1(esLogLevelEnum, "SearchRequest -> DSL:{}", searchSourceBuilder.toString());
        } else if (request instanceof CountRequest) {
            CountRequest countRequest = (CountRequest) request;
            SearchSourceBuilder searchSourceBuilder = countRequest.source();
            esLog1(esLogLevelEnum, "CountRequest -> DSL:{}", searchSourceBuilder.toString());
        } else if (request instanceof IndexRequest) {
            IndexRequest indexRequest = (IndexRequest) request;
            esLog1(esLogLevelEnum, "indexRequest -> DSL:{}", indexRequest.toString());
        } else if (request instanceof GetRequest) {
            GetRequest getRequest = (GetRequest) request;
            esLog1(esLogLevelEnum, "getRequest -> DSL:{}", getRequest.toString());
        } else if (request instanceof ClearScrollRequest) {
            ClearScrollRequest clearScrollRequest = (ClearScrollRequest) request;
            List<String> scrollIds = clearScrollRequest.getScrollIds();
            esLog1(esLogLevelEnum, "clearScrollRequest -> DSL:{},scrollIds={}", clearScrollRequest.toString(), JSONUtil.toJsonStr(scrollIds));
        } else if (request instanceof SearchScrollRequest) {
            SearchScrollRequest searchScrollRequest = (SearchScrollRequest) request;
            esLog1(esLogLevelEnum, "searchScrollRequest -> DSL:{}", searchScrollRequest.toString());
        } else if (request instanceof DeleteRequest) {
            DeleteRequest deleteRequest = (DeleteRequest) request;
            esLog1(esLogLevelEnum, "deleteRequest -> DSL:{}", deleteRequest.toString());
        } else if (request instanceof DeleteByQueryRequest) {
            DeleteByQueryRequest deleteByQueryRequest = (DeleteByQueryRequest) request;
            esLog1(esLogLevelEnum, "deleteByQueryRequest -> DSL:{}", deleteByQueryRequest.toString());
        } else if (request instanceof UpdateRequest) {
            UpdateRequest updateRequest = (UpdateRequest) request;
            esLog1(esLogLevelEnum, "updateRequest -> DSL:{}", updateRequest.toString());
        } else if (request instanceof UpdateByQueryRequest) {
            UpdateByQueryRequest updateByQueryRequest = (UpdateByQueryRequest) request;
            esLog1(esLogLevelEnum, "updateByQueryRequest -> DSL:{}", updateByQueryRequest.toString());
        }
    }

    private static void esLog1(ElasticSearchConst.ESLogLevelEnum esLogLevelEnum, String var1, Object... var2) {
        switch (esLogLevelEnum) {
            case DEBUG:
                log.debug(var1, var2);
                break;
            case INFO:
                log.info(var1, var2);
                break;
            case ERROR:
                log.error(var1, var2);
                break;
            default:
                log.warn(var1, var2);
        }
    }

}
