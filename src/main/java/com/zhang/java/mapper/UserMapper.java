package com.zhang.java.mapper;

import com.zhang.java.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Date 2022/4/3 18:46
 * @Author zsy
 * @Description
 */
@Mapper
public interface UserMapper {
    User selectUserById(@Param("id") Integer id);

    User selectUserByName(@Param("username") String username);

    User selectUserByEmail(@Param("email") String email);

    Integer insertUser(User user);

    Integer updateUserStatus(@Param("id") Integer id, @Param("status") Integer status);

    Integer updateUserHeader(@Param("id") Integer id, @Param("headerUrl") String headerUrl);

    Integer updateUserPassword(@Param("id") Integer id, @Param("password") String password);
}
