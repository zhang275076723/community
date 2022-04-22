package com.zhang.java.service.impl;

import com.zhang.java.domain.Comment;
import com.zhang.java.mapper.CommentMapper;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.service.CommentService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

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

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Comment> findCommentsByEntity(Integer entityType, Integer entityId) {
        return commentMapper.selectCommentsByEntity(entityType, entityId);
    }

    @Override
    public Integer findCommentsCount(Integer entityType, Integer entityId) {
        return commentMapper.selectCommentsCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    @Override
    public Integer addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        //转义评论中的html标记，防止被浏览器解析
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //过滤评论中的敏感词
        comment.setContent(sensitiveFilter.filterSensitiveWords(comment.getContent()));

        //添加评论
        Integer row = commentMapper.insertComment(comment);

        //只有是对帖子的评论才更新帖子中评论数量
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_DISCUSSPOST) {
            Integer commentCount = commentMapper.selectCommentsCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateDiscussPostCommentCountById(comment.getEntityId(), commentCount);
        }

        return row;
    }
}
