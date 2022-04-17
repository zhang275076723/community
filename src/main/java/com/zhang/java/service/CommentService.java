package com.zhang.java.service;

import com.zhang.java.domain.Comment;

import java.util.List;

/**
 * @Date 2022/4/16 16:38
 * @Author zsy
 * @Description
 */
public interface CommentService {
    List<Comment> findCommentsByEntity(Integer entityType, Integer entityId);

    Integer findCommentsCount(Integer entityType, Integer entityId);

    Integer addComment(Comment comment);
}
