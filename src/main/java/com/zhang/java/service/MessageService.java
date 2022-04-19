package com.zhang.java.service;

import com.zhang.java.domain.Message;

import java.util.List;

/**
 * @Date 2022/4/17 17:08
 * @Author zsy
 * @Description
 */
public interface MessageService {
    List<Message> findConversations(Integer userId);

    Integer findConversationCount(Integer userId);

    List<Message> findLetters(String conversationId);

    Integer findLetterCount(String conversationId);

    Integer findLetterUnreadCount(Integer userId, String conversationId);

    Integer addMessage(Message message);

    Integer readUnReadMessage(List<Integer> ids);
}
