package com.xh.es.test;

import cn.hutool.json.JSONUtil;
import com.xh.es.common.constant.ElasticSearchConst;
import com.xh.es.model.UserEntity;
import com.xh.es.model.dto.*;
import com.xh.es.service.ElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author H.Yang
 * @date 2022/11/22
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticSearchTest {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Test
    public void getById() throws IOException {
        UserEntity demo = (UserEntity) elasticSearchService.getDocument(UserEntity.class, "demo", "1");
        log.info("根据ID查询:{}", demo);
    }


    @Test
    public void createOne() throws IOException {
        UserEntity entity = this.getUser();

        log.info(JSONUtil.toJsonStr(entity));
        boolean result = elasticSearchService.createDocument(entity, "demo", "1");
        log.info("添加结果:{}", result);
    }


    @Test
    public void createTwo() throws IOException {
        UserEntity entity = this.getUser();

        UserEntity demo = (UserEntity) elasticSearchService.createDocument(UserEntity.class, entity, "demo", "2");
        log.info("添加结果:{}", JSONUtil.toJsonStr(demo));
    }

    @Test
    public void addBatch() throws IOException {
        Map<String, Object> map = new HashMap<>();
        for (long i = 1L; i <= 21L; i++) {
            map.put(String.valueOf(i), this.getUser());
        }
        log.info("批量添加文档结果:{}", elasticSearchService.addBatchDocument(map, "demo"));
    }

    @Test
    public void deleteById() throws IOException {
        boolean del = elasticSearchService.deleteDocument("demo", "1");
        log.info("删除成功:{}", del);
    }

    /**
     * 根据单个条件删除(Term 相等条件)
     */
    @Test
    public void deleteByOne() throws IOException {
        String key = "des.keyword";
        Object val = "测试添加";
        Long delResult = elasticSearchService.deleteDocument("demo", key, val);
        log.info("根据单条件删除文档数量:{}", delResult);
    }


    @Test
    public void getListByAndMap() throws IOException {
        Map<String, Object> map = new HashMap<>();
        // 当des是keyword时,k -> des.keyword term才可以匹配到
        map.put("des.keyword", "测试添加");
        // 不加sort
        List<UserEntity> list = elasticSearchService.getListByAndMap(UserEntity.class, "demo", map);
        log.info("通过(map)全匹配结果,不加sort; size:{},list:{}", list.size(), JSONUtil.toJsonStr(list));

        // 带有sort
        HashMap<String, SortOrder> sortOrderHashMap = new HashMap<>();
        // 当des是keyword时,k -> des.keyword ,这样就不会报错
        sortOrderHashMap.put("id", SortOrder.DESC);
        list = elasticSearchService.getListByAndMap(UserEntity.class, "demo", map, sortOrderHashMap);
        log.info("通过(map)全匹配结果,带有sort; size:{},list:{}", list.size(), JSONUtil.toJsonStr(list));
    }

    @Test
    public void getListCondition() throws IOException {
        // or 语句 假设我要查询 name = 名称2 or name = 名称1
        List<ElasticSearchConditionDTO> conditionDos = new ArrayList<>();
        ElasticSearchConditionDTO build1 = ElasticSearchConditionDTO.builder()
                .k("name.keyword")
                .v("名称2")
                .and(false)
                .operation(ElasticSearchConst.ESOperationEnum.TERM_QUERY)
                .build();
        conditionDos.add(build1);
        ElasticSearchConditionDTO build2 = ElasticSearchConditionDTO.builder()
                .k("name.keyword")
                .v("名称1")
                .and(false)
                .operation(ElasticSearchConst.ESOperationEnum.TERM_QUERY)
                .build();
        conditionDos.add(build2);

//        // and 语句 假设我要查 des = 测试添加 && name = 名称2
//        conditionDos.clear();
//        ESConditionDTO build3 = ESConditionDTO.builder()
//                .k("des.keyword")
//                .v("测试添加")
//                .and(true)
//                .operation(ESConst.ESOperationEnum.TERM_QUERY)
//                .build();
//        conditionDos.add(build3);
//        ESConditionDTO build4 = ESConditionDTO.builder()
//                .k("name.keyword")
//                .v("名称2")
//                .and(true)
//                .operation(ESConst.ESOperationEnum.TERM_QUERY)
//                .build();
//        conditionDos.add(build4);
//
//        // and or 组合 总结果: and的结果就是总结果
//        // 1: 假设我要查询 and des = 测试添加,or name = 名称1
//        // 总结: and 查出的结果就是总结果
//        conditionDos.clear();
//        ESConditionDTO build5 = ESConditionDTO.builder()
//                .k("des.keyword")
//                .v("测试添加")
//                .and(true)
//                .operation(ESConst.ESOperationEnum.TERM_QUERY)
//                .build();
//        conditionDos.add(build5);
//        ESConditionDTO build6 = ESConditionDTO.builder()
//                .k("name.keyword")
//                .v("名称1")
//                .and(false)
//                .operation(ESConst.ESOperationEnum.TERM_QUERY)
//                .build();
//        conditionDos.add(build6);
//        // 2: 假设我要查询 and name = 名称1,or name = 名称2
//        // 总结: and 查出的结果就是总结果
//        conditionDos.clear();
//        ESConditionDTO build7 = ESConditionDTO.builder()
//                .k("name.keyword")
//                .v("名称1")
//                .and(true)
//                .operation(ESConst.ESOperationEnum.TERM_QUERY)
//                .build();
//        conditionDos.add(build7);
//        ESConditionDTO build8 = ESConditionDTO.builder()
//                .k("name.keyword")
//                .v("名称2")
//                .and(false)
//                .operation(ESConst.ESOperationEnum.TERM_QUERY)
//                .build();
//        conditionDos.add(build8);

        // 不带排序
//        List list = elasticsearchService.getListByCondition(DemoEsDTO.class, "demo", conditionDos);
        // 带排序
        ElasticSearchSearchDTO esSearchDto = new ElasticSearchSearchDTO(conditionDos);
        HashMap<String, SortOrder> sortOrderHashMap = new HashMap<>();
        sortOrderHashMap.put("id", SortOrder.ASC);
        esSearchDto.setSortOrderMap(sortOrderHashMap);
        List list = elasticSearchService.getListByCondition(UserEntity.class, "demo", esSearchDto);
        log.info("通过自定义条件DTO查询到的结果,size:{},list:{}", list.size(), list);
    }


    /**
     * ps -> es 分页需知 https://www.jianshu.com/p/733e7e1e4de5
     * 这里感谢 {小胖学编程} https://www.jianshu.com/u/106c065a14f4
     * 分页很多都有借鉴他的思想。
     * <p>
     * 测试三种分页分页,三种分页共同搜索条件
     */
    private ElasticSearchSearchDTO getSearchDto() {
        List<ElasticSearchConditionDTO> conditionDTOS = new ArrayList<>();
        ElasticSearchConditionDTO build5 = ElasticSearchConditionDTO.builder()
                .k("des.keyword")
                .v("测试添加")
                .and(true)
                .operation(ElasticSearchConst.ESOperationEnum.TERM_QUERY)
                .build();
        conditionDTOS.add(build5);
        Map<String, SortOrder> sortOrderMap = new HashMap<>();
        sortOrderMap.put("id", SortOrder.ASC);
        return new ElasticSearchSearchDTO(conditionDTOS, sortOrderMap);
    }

    /**
     * FromSize -> 指定跳转分页 page=0表示从第一页开始,page=10表示第二页,一次类推,每加一页 page=page+10
     */
    @Test
    public void getFromSizePage() throws IOException {
        // 指定跳转分页 - from-size
        ElasticSearchSearchDTO esSearchDto = this.getSearchDto();
        int page = 0;
        RequestFromSizePage requestFromSizePage = RequestFromSizePage.of(page, 10);
        Page<UserEntity> pageFromSizeByCondition = elasticSearchService.getPageFromSizeByCondition(UserEntity.class, "demo", esSearchDto, requestFromSizePage);
        do {
            log.info("本次分页参数:{},结果,size:{},page:{}", requestFromSizePage, pageFromSizeByCondition.getContent().size(), pageFromSizeByCondition);
            requestFromSizePage.setPage(requestFromSizePage.getPage() + 10);
            pageFromSizeByCondition = elasticSearchService.getPageFromSizeByCondition(UserEntity.class, "demo", esSearchDto, requestFromSizePage);
        } while (!CollectionUtils.isEmpty(pageFromSizeByCondition.getContent()));
    }

    /**
     * Scroll -> 查询满足条件的所有数据, 实际上该分页已不可以分页,查出的内容为全部
     * <p>
     * 注意: 我当前已经直接查询所有数据了,为了满足list all,如果需要实现分页的效果,
     * 1： 请自己重写 {@link com.xh.es.service.strategy.ElasticSearchRequestPageStrategy}
     * 2： 请修改 {@link com.xh.es.service.ElasticSearchService} 下的 list()
     * 3:  入参时,scrollTimeValue 必须设置的时间长一点,不然快照就过期了,但是这样会导致空间被大量占用
     */
    @Test
    public void getScrollPage() throws IOException {
        ElasticSearchSearchDTO esSearchDto = this.getSearchDto();
        RequestScrollPage requestScrollPage = RequestScrollPage.of(10, TimeValue.timeValueMinutes(2));
        Page<UserEntity> page = elasticSearchService.getPageByCondition(UserEntity.class, "demo", esSearchDto, requestScrollPage);
        log.info("本次分页结果,size:{},page:{}", page.getContent().size(), page);
    }

    /**
     * SearchAfter ->
     * 该方法只建议用于滚动的场景,不可以指定跳转页数
     * 例如app的下翻,只能回到顶部那种,回到顶部不要传 Object[] values,就会直接默认查询从 page=0 ~ limit,
     * 该方法只能指定page=0(es 默认page=0，limit=10),所以你传page也没用,我不会设置该参数,
     * 如果想要实现首页的跳转,那么你可以你自己实现传输page试试! 我没试过!
     * <p>
     * 用例解析: 也就是我为什么要怎么写?
     * <p>
     * RequestSearchAfterPage.of(10, "name.keyword", SortOrder.ASC,null)
     * // 当 values 为null时,表示你是要进行首页查询(page=0,limit)
     * <p>
     * while (!CollectionUtils.isEmpty(page.getContent()));
     * // 表示下一页内容是否没有了,也就是页面上的到底了,一滴都没有了,大兄弟,没有了
     * <p>
     * elasticsearchService.getPageByCondition(DemoEsVo.class, "demo", esSearchDto, requestSearchAfterPage);
     * // 为什么我的文档类是 {@link DemoEsDTO} 用的却是 {@link DemoEsVo},这是因为SearchAfter的特殊性,当你要查询下一页时
     * 你必须的有上页某个文档的sort属性( 也就是这里的{@link DemoEsVo} -> values ),你得把它返回给用户,在他要往下翻页的时候
     * 再传回来入参 {@link com.blacktea.es.service.strategy.SearchAfterPageStrategyImpl}
     * 里的 -> {searchSourceBuilder.searchAfter(values);}
     * 作为 search_after 的入参
     */
    @Test
    public void getSearchAfterPage() throws IOException {
        ElasticSearchSearchDTO esSearchDto = this.getSearchDto();
        RequestSearchAfterPage requestSearchAfterPage = RequestSearchAfterPage.of(10, "name.keyword", SortOrder.ASC, null);
        Page<DemoEsVo> page = elasticSearchService.getPageByCondition(DemoEsVo.class, "demo", esSearchDto, requestSearchAfterPage);
        do {
            log.info("本次分页结果,size:{},page:{}", page.getContent().size(), JSON.toJSONString(page));
            Object[] sort = page.getContent().get(page.getContent().size() - 1).getSort();
            requestSearchAfterPage.setValues(sort);
            page = elasticsearchService.getPageByCondition(DemoEsVo.class, "demo", esSearchDto, requestSearchAfterPage);
        } while (!CollectionUtils.isEmpty(page.getContent()));
    }


    private UserEntity getUser() {
        long millis = System.currentTimeMillis();

        UserEntity entity = new UserEntity();

        entity.setUserId(millis);
        entity.setUsername("es-" + millis);
        entity.setAccount(millis + "");
        entity.setCreatedAt(new Date());

        return entity;
    }
}
