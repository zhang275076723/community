package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/17 16:40
 * @Author zsy
 * @Description 私信，包括私信列表和私信列表中的每一条私信
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private int id;
    private int fromId;
    private int toId;
    //私信双方，111-112
    private String conversationId;
    private String content;
    //私信状态，0-未读，1-已读，2-删除
    private int status;
    private Date createTime;
}
