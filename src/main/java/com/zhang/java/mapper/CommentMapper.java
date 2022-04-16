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
    List<Comment> selectCommentsByEntity(@Param("entityType") Integer entityType,
                                         @Param("entityId") Integer entityId);

    Integer selectCommentsCountByEntity(@Param("entityType")Integer entityType,
                                        @Param("entityId")Integer entityId);
}
