package com.zhang.java.service;

import com.zhang.java.domain.Message;

import java.util.List;

/**
 * @Date 2022/4/17 17:08
 * @Author zsy
 * @Description
 */
public interface MessageService {
    /**
     * 查询用户的私信列表，每个私信列表只返回最新一条的私信
     *
     * @param userId
     * @return
     */
    List<Message> findConversations(Integer userId);

    /**
     * 查询用户的私信列表数量
     *
     * @param userId
     * @return
     */
    Integer findConversationCount(Integer userId);

    /**
     * 查询某个私信列表中的私信
     *
     * @param conversationId
     * @return
     */
    List<Message> findLetters(String conversationId);

    /**
     * 查询某个私信列表中的私信数量
     *
     * @param conversationId
     * @return
     */
    Integer findLetterCount(String conversationId);

    /**
     * 1、查询查询用户未读私信的数量，conversationId为null
     * 2、查询用户某个私信列表未读私信的数量，conversationId不为null
     *
     * @param userId
     * @param conversationId
     * @return
     */
    Integer findLetterUnreadCount(Integer userId, String conversationId);

    /**
     * 发送消息，转义消息中的html标记，防止被浏览器解析，并过滤敏感词
     *
     * @param message
     * @return
     */
    Integer addMessage(Message message);

    /**
     * 根据id批量修改消息的状态为已读
     *
     * @param ids
     * @return
     */
    Integer readUnReadMessage(List<Integer> ids);

    /**
     * 查询某个主题最新的系统通知
     *
     * @param userId
     * @param topic
     * @return
     */
    Message findLastestNotice(int userId, String topic);

    /**
     * 查询某个主题所包含的通知数量
     *
     * @param userId
     * @param topic
     * @return
     */
    int findNoticeCount(int userId, String topic);

    /**
     * 1、查询某个主题未读的通知的数量，topic不为null
     * 2、查询所有主题未读的通知的数量，topic为null
     *
     * @param userId
     * @param topic
     * @return
     */
    int findNoticeUnreadCount(int userId, String topic);

    /**
     * 查询某个主题所有的系统通知
     *
     * @param userId
     * @param topic
     * @return
     */
    List<Message> findNotices(int userId, String topic);
}
