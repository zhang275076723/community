package com.zhang.java.service;

import com.zhang.java.domain.LoginTicket;
import com.zhang.java.domain.User;

import java.util.Map;

/**
 * @Date 2022/4/3 20:25
 * @Author zsy
 * @Description
 */
public interface UserService {
    User findUserById(Integer id);

    Map<String, Object> login(String username, String password, Integer expiredSeconds);

    void logout(String ticket);

    Map<String, Object> register(User user);

    Integer activation(Integer userId, String activationCode);

    LoginTicket findLoginTicketByTicket(String ticket);
}
