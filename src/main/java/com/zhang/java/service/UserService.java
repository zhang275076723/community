package com.zhang.java.service;

import com.zhang.java.domain.User;

import java.util.Map;

/**
 * @Date 2022/4/3 20:25
 * @Author zsy
 * @Description
 */
public interface UserService {
    User findUserById(int id);

    Map<String,Object> register(User user);

    int activation(int userId, String activationCode);
}
