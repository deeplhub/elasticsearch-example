package com.xh.es.model.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.StringUtils;


/**
 * @description: Search After 分页
 *                  可以在实时的情况下处理深度分页，在Es5.x版本后提供的功能，search_after缺点是不能够随机跳转分页，
 *                  只能是一页一页的向后翻，并且需要至少指定一个唯一不重复字段来排序。
 *                  注意：search_after必须指定一个唯一不重复的字段来排序，此处我们指定了两个字段进行排序。
 * @author: black tea
 * @date: 2021/9/7 10:23
 */
@Data
@ToString
public class RequestSearchAfterPage extends PageRequest{

    /** 唯一标识 **/
    private String unique;

    /** 排序方式 **/
    private SortOrder sortOrder;

    /** 上一次查询最后一个文档的sort,用于下也一个查询 **/
    private Object[] values;

    private RequestSearchAfterPage(){}

    private RequestSearchAfterPage(int limit){
        this.limit = limit;
    }

    public static RequestSearchAfterPage of(int limit, String unique, SortOrder sortOrder, Object[] values){
        if (!StringUtils.hasLength(unique) || null == sortOrder) {
//            AssertUtil.fail(ESConst.ESErrorCodeEnum.PAGE_REQUEST_SEARCHAFTER_PARAM_NOT_NULL);
        }
        RequestSearchAfterPage requestSearchAfterPage = new RequestSearchAfterPage(limit);
        requestSearchAfterPage.setUnique(unique);
        requestSearchAfterPage.setSortOrder(sortOrder);
        requestSearchAfterPage.setValues(values);
        return requestSearchAfterPage;
    }

    public FieldSortBuilder getSort(){
        return SortBuilders.fieldSort(unique).order(sortOrder);
    }

}
