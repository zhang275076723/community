package com.zhang.java.service.Impl;

import com.zhang.java.domain.User;
import com.zhang.java.mapper.UserMapper;
import com.zhang.java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Date 2022/4/3 20:28
 * @Author zsy
 * @Description
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;


    @Override
    public User findUserById(int id) {
        return userMapper.selectUserById(id);
    }
}
