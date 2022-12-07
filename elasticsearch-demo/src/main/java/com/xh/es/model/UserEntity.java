package com.xh.es.model;

import lombok.Data;

import java.util.Date;

/**
 * @author H.Yang
 * @date 2022/11/22
 */
@Data
public class UserEntity {

    private Long userId;

    private String username;

    /**
     * 用户别称
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建日期
     */
    private Date createdAt;


}
