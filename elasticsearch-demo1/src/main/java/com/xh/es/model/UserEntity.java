package com.xh.es.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author H.Yang
 * @date 2022/11/30
 */
@Data
@Builder
public class UserEntity {

    private String name;
    private String sex;
    private Integer age;
}
