package com.zhang.java.mapper;

import com.zhang.java.domain.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @Date 2022/4/11 16:04
 * @Author zsy
 * @Description 用户登录凭证，使用redis存储替代存储在数据库中
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {
    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectLoginTicketByTicket(String ticket);

    Integer updateLoginTicketStatus(String ticket, Integer status);
}
