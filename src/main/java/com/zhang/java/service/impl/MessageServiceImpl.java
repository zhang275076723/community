package com.zhang.java.service.impl;

import com.zhang.java.domain.Message;
import com.zhang.java.mapper.MessageMapper;
import com.zhang.java.service.MessageService;
import com.zhang.java.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

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

    @Autowired
    private SensitiveFilter sensitiveFilter;

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

    @Override
    public Integer addMessage(Message message) {
        //转义私信中的html标记，防止被浏览器解析
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        //过滤私信中的敏感词
        message.setContent(sensitiveFilter.filterSensitiveWords(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    @Override
    public Integer readUnReadMessage(List<Integer> ids) {
        return messageMapper.updateMessageStatus(ids, 1);
    }
}
