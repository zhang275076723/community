package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/3 18:43
 * @Author zsy
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;
    private String salt;
    private String email;
    //用户类型，0-普通用户，1-超级管理员，2-版主
    private Integer type;
    //用户状态，0-未激活; 1-已激活
    private Integer status;
    private String activationCode;
    //用户头像url，0-1000t.png
    private String headerUrl;
    private Date createTime;
}
