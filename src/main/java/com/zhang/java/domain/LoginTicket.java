package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/11 16:02
 * @Author zsy
 * @Description 用户登录凭证
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginTicket {
    /**
     * 凭证id
     */
    private Integer id;

    /**
     * 凭证用户id
     */
    private Integer userId;

    /**
     * 登录凭证，32位的十六进制数
     */
    private String ticket;

    /**
     * 凭证状态：
     * 0-有效
     * 1-失效
     */
    private Integer status;

    /**
     * 凭证到期时间
     */
    private Date expired;
}
