package com.xh.es.test;

import com.xh.es.example.model.ProductEntity;
import com.xh.es.example.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档操作
 *
 * @author H.Yang
 * @date 2022/12/7
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductRepositoryTest {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private ProductRepository productRepository;

    //创建索引并增加映射配置
    @Test
    public void createIndex() {
        //创建索引，系统初始化会自动创建索引
        System.out.println("创建索引");
    }

    @Test
    public void deleteIndex() {
        //创建索引，系统初始化会自动创建索引
        boolean flg = restTemplate.deleteIndex(ProductEntity.class);
        System.out.println("删除索引 = " + flg);
    }


    @Test
    public void save() {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(1L);
        productEntity.setTitel("华为手机");
        productEntity.setCategory("手机");
        productEntity.setPrice(299.0);
        productEntity.setImages("http://sjdfsgjkfdsghdjkfg");

        productRepository.save(productEntity);
    }

    @Test
    public void update() {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(1L);
        productEntity.setTitel("华为2手机");
        productEntity.setCategory("手机");
        productEntity.setPrice(299.0);
        productEntity.setImages("http://sjdfsgjkfdsghdjkfg");

        productRepository.save(productEntity);
    }

    //根据 id 查询
    @Test
    public void findById() {
        ProductEntity product = productRepository.findById(1L).get();
        System.out.println(product);
    }

    /**
     * 查询所有
     */
    @Test
    public void findAll() {
        Iterable<ProductEntity> products = productRepository.findAll();
        for (ProductEntity product : products) {
            System.out.println(product);
        }
    }

    //删除
    @Test
    public void delete() {
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        productRepository.delete(product);
    }

    //批量新增
    @Test
    public void saveAll() {
        List<ProductEntity> productList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ProductEntity product = new ProductEntity();
            product.setId(Long.valueOf(i));
            product.setTitel("[" + i + "]小米手机");
            product.setCategory("手机");
            product.setPrice(1999.0 + i);
            product.setImages("http://www.atguigu/xm.jpg");
            productList.add(product);
        }
        productRepository.saveAll(productList);
    }

    //分页查询
    @Test
    public void findByPageable() {
        //设置排序(排序方式，正序还是倒序，排序的 id)
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        int currentPage = 0;//当前页，第一页从 0 开始，1 表示第二页
        int pageSize = 5;//每页显示多少条
        //设置查询分页
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, sort);
        //分页查询
        Page<ProductEntity> productPage = productRepository.findAll(pageRequest);
        for (ProductEntity ProductEntity : productPage.getContent()) {
            System.out.println(ProductEntity);
        }
    }

}
