package com.zhang.java.mapper;

import com.zhang.java.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @Date 2022/4/3 18:46
 * @Author zsy
 * @Description
 */
@Mapper
public interface UserMapper {
    User selectUserById(@Param("id") int id);

    User selectUserByName(@Param("username") String username);

    User selectUserByEmail(@Param("email") String email);

    int insertUser(User user);

    int updateUserStatus(@Param("id") int id, @Param("status") int status);

    int updateUserHeader(@Param("id") int id, @Param("headerUrl") String headerUrl);

    int updateUserPassword(@Param("id") int id, @Param("password") String password);
}
