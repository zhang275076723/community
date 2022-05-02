package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/17 16:40
 * @Author zsy
 * @Description 朋友私信和系统通知
 * 包括：
 * 1、私信列表
 * 2、私信列表中的每一条私信
 * 3、系统在评论、点赞、关注时发送的通知
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
     * 发送方id，id为1表示系统通知
     */
    private int fromId;

    /**
     * 接收方id
     */
    private int toId;

    /**
     * 消息双方id，冗余字段，格式：111-112
     * 如果fromId为1，则为系统通知，存放主题类型：评论、点赞、关注
     */
    private String conversationId;

    /**
     * 消息内容
     * 如果fromId为1，则存放json格式的系统通知
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
