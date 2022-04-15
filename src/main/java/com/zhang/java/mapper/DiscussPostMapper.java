package com.zhang.java.mapper;

import com.zhang.java.domain.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Date 2022/4/3 19:34
 * @Author zsy
 * @Description
 */
@Mapper
public interface DiscussPostMapper {
    /**
     * 查询帖子，如果userId为0，表示查询全部帖子
     *
     * @param userId
     * @return
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId);

    /**
     * 查询帖子的数量，如果userId为0，表示查询全部帖子的数量
     *
     * @param userId
     * @return
     */
    Integer selectDiscussPostRows(@Param("userId") Integer userId);

    /**
     * 添加帖子
     *
     * @param discussPost
     * @return
     */
    Integer insertDiscussPost(DiscussPost discussPost);


    /**
     * 根据id查询帖子
     *
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(@Param("id") Integer id);
}
