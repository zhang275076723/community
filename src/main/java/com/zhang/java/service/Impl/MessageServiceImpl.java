package com.zhang.java.service.Impl;

import com.zhang.java.domain.Message;
import com.zhang.java.mapper.MessageMapper;
import com.zhang.java.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Date 2022/4/17 17:08
 * @Author zsy
 * @Description
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> findConversations(Integer userId) {
        return messageMapper.selectConversations(userId);
    }

    @Override
    public Integer findConversationCount(Integer userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public List<Message> findLetters(String conversationId) {
        return messageMapper.selectLetters(conversationId);
    }

    @Override
    public Integer findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    @Override
    public Integer findLetterUnreadCount(Integer userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }
}
