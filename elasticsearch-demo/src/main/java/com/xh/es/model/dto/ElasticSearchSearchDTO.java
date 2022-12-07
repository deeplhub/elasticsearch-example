package com.xh.es.model.dto;

import lombok.Data;
import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: black tea
 * @date: 2021/9/7 10:00
 */
@Data
public class ElasticSearchSearchDTO {

    private List<ElasticSearchConditionDTO> conditionDTOS;

    private Map<String, SortOrder> sortOrderMap;

    public ElasticSearchSearchDTO(List<ElasticSearchConditionDTO> conditionDTOS) {
        this.conditionDTOS = conditionDTOS;
    }

    public ElasticSearchSearchDTO(List<ElasticSearchConditionDTO> conditionDTOS, Map<String, SortOrder> sortOrderMap) {
        this.conditionDTOS = conditionDTOS;
        this.sortOrderMap = sortOrderMap;
    }
}
