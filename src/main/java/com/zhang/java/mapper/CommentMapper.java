package com.zhang.java.mapper;

import com.zhang.java.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Date 2022/4/16 16:31
 * @Author zsy
 * @Description
 */
@Mapper
public interface CommentMapper {
    /**
     * 根据实体类型和实体id查询评论
     *
     * @param entityType
     * @param entityId
     * @return
     */
    List<Comment> selectCommentsByEntity(@Param("entityType") Integer entityType,
                                         @Param("entityId") Integer entityId);

    /**
     * 根据实体类型和实体id查询评论数量
     * @param entityType
     * @param entityId
     * @return
     */
    Integer selectCommentsCountByEntity(@Param("entityType") Integer entityType,
                                        @Param("entityId") Integer entityId);

    /**
     * 添加评论
     * @param comment
     * @return
     */
    Integer insertComment(Comment comment);
}
