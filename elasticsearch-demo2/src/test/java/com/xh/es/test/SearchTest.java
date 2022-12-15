package com.xh.es.test;

import com.xh.es.example.model.ProductEntity;
import com.xh.es.example.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 文档搜索
 *
 * @author H.Yang
 * @date 2022/12/8
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * term 查询
     * search(termQueryBuilder) 调用搜索方法，参数查询构建器对象
     */
    @Test
    public void termQuery() {
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", "小米");
        Iterable<ProductEntity> products = productRepository.search(termQueryBuilder);
        for (ProductEntity product : products) {
            System.out.println(product);
        }
    }

    /**
     * term 查询加分页
     */
    @Test
    public void termQueryByPage() {
        int currentPage = 0;
        int pageSize = 5;
        //设置查询分页
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", "小米");
        Iterable<ProductEntity> products = productRepository.search(termQueryBuilder, pageRequest);
        for (ProductEntity product : products) {
            System.out.println(product);
        }
    }

    @Test
    public void demo() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

//        构建查询
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("category", "小米"))
                .withQuery(QueryBuilders.rangeQuery("price").lte(2000))
                .build();


    }


}
