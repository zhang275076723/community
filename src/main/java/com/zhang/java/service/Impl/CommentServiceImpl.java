package com.zhang.java.service.Impl;

import com.zhang.java.domain.Comment;
import com.zhang.java.mapper.CommentMapper;
import com.zhang.java.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Date 2022/4/16 16:39
 * @Author zsy
 * @Description
 */
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public List<Comment> findCommentsByEntity(Integer entityType, Integer entityId) {
        return commentMapper.selectCommentsByEntity(entityType, entityId);
    }

    @Override
    public Integer findCommentsCount(Integer entityType, Integer entityId) {
        return commentMapper.selectCommentsCountByEntity(entityType, entityId);
    }
}
