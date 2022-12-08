package com.xh.es.example.repository;

import com.xh.es.example.model.ProductEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author H.Yang
 * @date 2022/12/7
 */
@Repository
public interface ProductRepository extends ElasticsearchRepository<ProductEntity, Long> {
}
