package com.xh.es.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author H.Yang
 * @date 2022/11/23
 */
public class ElasticSearchConst {

    /**
     * 常用值
     **/
    public static int ZERO = 0;
    /**
     * maxCount = from-size分页查询总量之和
     **/
    public static int MAX_FROMSIZE_COUNT = 10000;
    /**
     * SCROLL分页每次默认是1000
     **/
    public static int SCROLL_LIMIT = 1000;


    @Getter
    @AllArgsConstructor
    public enum ESLogLevelEnum {
        /**
         * {@link com.xh.es.common.constant.ElasticSearchConst}  esLog、esOperationLog 方法 的打印级别
         **/
        DEBUG(2),
        INFO(3),
        ERROR(4);
        private final int level;

        public static ESLogLevelEnum getByLevel(int level) {
            return Arrays.stream(ESLogLevelEnum.values())
                    .filter(l -> l.getLevel() == level)
                    .findFirst()
                    .orElseGet(() -> DEBUG);
        }
    }

    public enum ESOperationEnum {
        /**
         * = 全等匹配
         **/
        TERM_QUERY,
        /**
         * like 模糊匹配
         **/
        MULTI_MATCH_QUERY;
    }

//    @Getter
//    @AllArgsConstructor
//    public enum ESErrorCodeEnum implements IErrorCode {
//        /**
//         * 分页错误
//         **/
//        PAGE_SCROLL_TIMEVALUE_NOT_NULL(50L, "ES使用SCROLL分页,必须要传入失效时间!"),
//        PAGE_REQUEST_NOT_NULL(51L, "ES查询分页时,分页对象不可以为空!"),
//        PAGE_REQUEST_SEARCHAFTER_PARAM_NOT_NULL(52L, "ES使用SEARCH_AFTER分页,必须传入唯一的标识并选择排序方式!"),
//
//        /**
//         * 该case缺少了对应的操作条件判断
//         **/
//        OPERATION_CONDITION_CASE_EMPTY(70L, "组合条件时,发现缺少对应的case逻辑!"),
//
//        /**
//         * Script相关错误
//         **/
//        SCRIPTDTO_IS_ERROR(101L, "SCRIPT组合错误!"),
//
//        /**
//         * es batch
//         **/
//        ES_BATCH_MAP_NOT_EMPTY(200L, "ES进行批量操作时,入参Map不可以为空或size为0!"),
//        ES_BATCH_IDS_NOT_EMPTY(201L, "ES根据_id集合进行批量操作时,ids不可以为空或size为0!");
//
//        private final long code;
//        private final String message;
//
//    }

}
