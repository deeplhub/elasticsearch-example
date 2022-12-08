package com.xh.es.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author H.Yang
 * @date 2022/12/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(indexName = "product")
public class ProductEntity {

    @Id
    private Long id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String titel;
    @Field(type = FieldType.Keyword)
    private String category;
    @Field(type = FieldType.Double)
    private Double price;
    @Field(type = FieldType.Keyword, index = false)
    private String images;
}
