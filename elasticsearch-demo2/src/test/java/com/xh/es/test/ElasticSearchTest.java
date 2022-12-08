package com.xh.es.test;

import com.xh.es.example.model.ProductEntity;
import com.xh.es.example.model.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author H.Yang
 * @date 2022/12/7
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticSearchTest {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private ProductRepository productRepository;


    @Test
    public void save(){
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(1L);
        productEntity.setTitel("华为手机");
        productEntity.setCategory("手机");
        productEntity.setPrice(299.0);
        productEntity.setImages("http://sjdfsgjkfdsghdjkfg");

        productRepository.save(productEntity);
    }
}
