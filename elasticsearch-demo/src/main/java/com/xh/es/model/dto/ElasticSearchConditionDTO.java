package com.xh.es.model.dto;

import com.xh.es.common.constant.ElasticSearchConst;
import lombok.Builder;
import lombok.Data;

/**
 * @description: Elasticsearch 条件组合DTO
 * 一般用于 {@link com.xh.es.service.ElasticSearchService} 下的接口实现 (XXXByCondition) 组合es查询条件
 * @author: black tea
 * @date: 2021/9/7 9:18
 */
@Data
@Builder
public class ElasticSearchConditionDTO {

    /**
     * 查询的字段名称
     **/
    private String k;
    /**
     * 查询字段值
     **/
    private String v;
    /**
     * 是否是 and 拼接
     **/
    private boolean and;
    /**
     * 操作类型, 例如 mysql中的 =或like
     **/
    private ElasticSearchConst.ESOperationEnum operation;
}
