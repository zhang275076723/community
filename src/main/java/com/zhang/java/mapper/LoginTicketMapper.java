package com.zhang.java.mapper;

import com.zhang.java.domain.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @Date 2022/4/11 16:04
 * @Author zsy
 * @Description
 */
@Mapper
public interface LoginTicketMapper {
    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectLoginTicketByTicket(String ticket);

    Integer updateLoginTicketStatus(String ticket, Integer status);
}
