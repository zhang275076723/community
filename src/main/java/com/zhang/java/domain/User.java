package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/3 18:43
 * @Author zsy
 * @Description 用户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /**
     * 用户id，id为1，表示系统管理员
     */
    private Integer id;

    private String username;

    /**
     * 密码，经过md5加密
     */
    private String password;

    /**
     * 用户输入的密码+salt，再经过md5加密，判断是否和密码一样
     */
    private String salt;

    private String email;

    /**
     * 用户类型：
     * 0-普通用户
     * 1-管理员，帖子删除权限
     * 2-版主。帖子置顶、加精权限
     */
    private Integer type;

    /**
     * 用户状态：
     * 0-未激活
     * 1-已激活
     */
    private Integer status;

    private String activationCode;
    /**
     * 用户头像url地址，0-1000t.png
     * 也可以使用本地上传的头像url
     */
    private String headerUrl;

    private Date createTime;
}
