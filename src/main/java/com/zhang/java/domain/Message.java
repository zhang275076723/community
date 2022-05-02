package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/17 16:40
 * @Author zsy
 * @Description 消息，包括消息列表和消息列表中的每一条消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    /**
     * 消息id
     */
    private int id;

    /**
     * 发送方id，id为1表示系统消息
     */
    private int fromId;

    /**
     * 接收方id
     */
    private int toId;

    /**
     * 消息双方id，冗余字段，格式：111-112
     * 如果fromId为1，则为系统消息，存放主题类型：评论、点赞、关注
     */
    private String conversationId;

    /**
     * 消息内容
     * 如果fromId为1，则存放json格式的系统消息
     */
    private String content;

    /**
     * 接收方的消息状态，0-未读，1-已读，2-删除
     */
    private int status;

    /**
     * 消息发送时间
     */
    private Date createTime;
}
