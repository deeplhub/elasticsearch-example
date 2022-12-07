package com.xh.es.service;

import com.xh.es.model.UserEntity;
import com.xh.es.model.dto.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 基于 RestHighLevelClient 封装的ElasticsearchService
 *
 * @author H.Yang
 * @date 2022/11/22
 */
public interface ElasticSearchService<T> {

    /**
     * 创建文档,并返回布尔值
     * 当索引不存在时,会自动创建
     *
     * @param var2  入参类(实际文档内容)
     * @param index 索引
     * @param id    id标识
     * @return true 成功
     * @throws IOException 异常
     */
    boolean createDocument(Object var2, String index, String id) throws IOException;

    /**
     * 创建文档,并返回创建成功的文档内容
     * 当索引不存在时,会自动创建
     *
     * @param var1  返回的对象类型
     * @param var2  入参类(实际文档内容)
     * @param index 索引
     * @param id    id标识
     * @return null 表示失败
     * @throws IOException 异常
     */
    T createDocument(Class<T> var1, Object var2, String index, String id) throws IOException;

    /**
     * 批量创建文档
     *
     * @param var   入参map -> k:id,v:文档内容
     * @param index 索引
     * @return boolean true 成功
     * @throws IOException 异常
     */
    boolean addBatchDocument(Map<String, Object> var, String index) throws IOException;

    /**
     * 通过id标识,删除文档,并返回布尔值
     * 不会删除索引
     *
     * @param index 索引
     * @param id    id标识
     * @return true 成功
     * @throws IOException 异常
     */
    boolean deleteDocument(String index, String id) throws IOException;

    /**
     * 根据 ids集合 批量删除文档
     *
     * @param index 索引
     * @param ids   _id 集合
     * @return boolean
     * @throws IOException 异常
     */
    boolean deleteBatchDocument(String index, List<String> ids) throws IOException;

    /**
     * 按单个条件(term)删除文档,并返回删除数量
     * 不会删除索引
     *
     * @param index 索引
     * @param key   fieldName 字段名称
     * @param value 字段值
     * @return Long 删除数量
     * @throws IOException 异常
     */
    long deleteDocument(String index, String key, Object value) throws IOException;


    /**
     * 根据多条件进行删除文档,并返回删除数量
     *
     * @param index         索引
     * @param conditionDTOS 自定义条件集合
     * @return Long 删除数量
     * @throws IOException 异常
     */
    long deleteDocumentByCondition(String index, List<ElasticSearchConditionDTO> conditionDTOS) throws IOException;

    /**
     * 根据id更新文档,并返回布尔值
     * 当索引不存在时,会自动创建
     *
     * @param var2  入参类(实际文档内容)
     * @param index 索引
     * @param id    id标识
     * @return true 成功
     * @throws IOException 异常
     */
    boolean updateDocument(Object var2, String index, String id) throws IOException;

    /**
     * 根据id更新文档,并返回T
     *
     * @param var1  返回的对象类型
     * @param var2  修改的内容文档
     * @param index 索引
     * @param id    id标识
     * @return T null 表示为修改失败
     * 失败原因:
     * 1: 该索引下不存在当前id;
     * 2: es修改返回 status = false;
     * @throws IOException
     */
    T updateDocument(Class<T> var1, Object var2, String index, String id) throws IOException;

    /**
     * 根据多条件进行文档更新,并返回更新数量
     *
     * @param var2          更新后的文档内容
     * @param index         索引
     * @param conditionDTOS 自定义条件集合
     * @return long 更新数量
     * @throws IOException 异常
     */
    long updateDocumentByCondition(Object var2, String index, List<ElasticSearchConditionDTO> conditionDTOS) throws IOException;

    /**
     * 根据 ScriptDto 进行文档更新,并返回更新数量
     *
     * @param scriptDto     {@link ScriptDTO} 对象,包含了需要修改的字段属性等信息
     * @param index         索引
     * @param conditionDTOS 条件集合(类似于 mysql where 条件)
     * @return long 更新数量
     * @throws IOException 异常 ESException（自定义）
     */
    long updateDocumentByCondition(ScriptDTO scriptDto, String index, List<ElasticSearchConditionDTO> conditionDTOS) throws IOException;

    /**
     * 根据Map -> k组成的id进行批量更新文档
     * Map 的 k 表示更新文档的 _id,
     * Map 的 v 表示更新文档的内容 _source
     *
     * @param index  索引
     * @param params 包含id和对应更新文档内容的Map
     * @return boolean
     * @throws IOException 异常
     */
    boolean updateBatchDocument(String index, Map<String, Object> params) throws IOException;

    /**
     * 通过索引下的id获取该文档内容
     *
     * @param var1  返回的对象类型
     * @param index 索引
     * @param id    id
     * @return T
     * @throws IOException 异常
     */
    T getDocument(Class<T> var1, String index, String id) throws IOException;

    /**
     * 根据 map 条件查询列表
     * 当前查询操作条件均为 and term
     *
     * @param var1  返回的对象类型
     * @param index 索引
     * @param map   条件map,当前条件操作全部设置为 term Query
     * @return List<T>
     * @throws IOException 异常
     */
    List<T> getListByAndMap(Class<T> var1, String index, Map<String, Object> map) throws IOException;

    /**
     * 根据 map 条件查询列表
     * 当前查询操作条件均为 and term
     *
     * @param var1         返回的对象类型
     * @param index        索引
     * @param map          条件map,当前条件操作全部设置为 term Query
     * @param sortOrderMap 排序Map -> k,v 分别表示排序 字段名称 和 值({@link org.elasticsearch.search.sort.SortOrder})
     * @return List<T>
     * @throws IOException 异常
     */
    List<T> getListByAndMap(Class<T> var1, String index, Map<String, Object> map, Map<String, SortOrder> sortOrderMap) throws IOException;

    /**
     * 根据 conditionDos 条件集合 去查询列表(仅拼接条件)
     * 因为es不支持查询全部,必须的分页查询,所以查询所有也是用的分页
     *
     * @param var1         返回的对象类型
     * @param index        索引
     * @param conditionDos 条件集合{@link ElasticSearchConditionDTO} 根据该集合对象进行组合
     * @return List<T>
     * @throws IOException 异常 ESException（自定义）
     */
    List<T> getListByCondition(Class<T> var1, String index, List<ElasticSearchConditionDTO> conditionDos) throws IOException;

    /**
     * 根据 searchDto 条件 去查询列表(包含拼接条件与排序)
     * 因为es不支持查询全部,必须的分页查询,所以查询所有也是用的分页
     *
     * @param var1      返回的对象类型
     * @param index     索引
     * @param searchDto {@link ElasticSearchSearchDTO} 根据该对象进行组合
     * @return List<T>
     * @throws IOException 异常 ESException（自定义）
     */
    List<T> getListByCondition(Class<T> var1, String index, ElasticSearchSearchDTO searchDto) throws IOException;

    /**
     * 根据条件进行分页查询
     * {@link PageRequest} 三种分页方案, 资料博客: https://blog.csdn.net/pony_maggie/article/details/105478557
     * 1: {@link com.xh.es.model.dto.RequestFromSizePage} from-size -> 占用空间大,可以指定页数,但是目前默认最大仅支持10000以内的分页
     * 2: {@link com.xh.es.model.dto.RequestScrollPage} scroll-> 效率高,不可以指定页数且非实时,但是可以查询大量数据,例如 10000以上的list查询使用!
     * 3: {@link com.xh.es.model.dto.RequestSearchAfterPage} Search_After -> 需要进行很深度的分页，但是可以不指定页数翻页，只要可以实时请求下一页就行。比如一些实时滚动的场景。
     * <p>
     * 注意: 不建议你去使用除 from-size 外的分页方法去实现指定页数跳转!
     *
     * @param var1        返回的对象类型
     * @param index       索引
     * @param esSearchDto {@link ElasticSearchSearchDTO} 根据该对象进行组合
     * @param pageRequest 分页对象,目前共三种 {@link PageRequest} 实现
     * @return Page<T>
     * @throws IOException 异常 ESException（自定义）
     */
    Page<T> getPageByCondition(Class<T> var1, String index, ElasticSearchSearchDTO esSearchDto, PageRequest pageRequest) throws IOException;

    /**
     * 根据条件进行分页查询
     * {@link com.xh.es.model.dto.RequestFromSizePage} from-size -> 占用空间大,可以指定页数,但是目前默认最大仅支持10000以内的分页
     *
     * @param var1                返回的对象类型
     * @param index               索引
     * @param esSearchDto         {@link ElasticSearchSearchDTO} 根据该对象进行组合
     * @param requestFromSizePage 分页对象,{@link RequestFromSizePage}
     * @return Page<T>
     * @throws IOException 异常 ESException（自定义）
     */
    Page<T> getPageFromSizeByCondition(Class<T> var1, String index, ElasticSearchSearchDTO esSearchDto, RequestFromSizePage requestFromSizePage) throws IOException;

    /**
     * 获取指定条件下的数据集合数量(array.size)
     *
     * @param idnex 索引
     * @param query 查询对象, 一般为 SearchSourceBuilder.queryBuilder
     * @return long
     * @throws IOException 异常
     */
    Long count(String idnex, QueryBuilder query) throws IOException;
}
