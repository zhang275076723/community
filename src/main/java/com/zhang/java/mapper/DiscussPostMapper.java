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
     * 查询帖子
     * userId为0，表示查询全部帖子
     *
     * @param userId    用户id
     * @param orderMode 排序模式，0-正常排序，1-按帖子分数由高到低排序
     * @param offset    查询偏移量
     * @param limit     查询数量
     * @return
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId,
                                         @Param("orderMode") int orderMode,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

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
     * 根据帖子id查询帖子
     *
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(@Param("id") Integer id);

    /**
     * 根据帖子id修改评论数量
     *
     * @param id
     * @param commentCount
     * @return
     */
    Integer updateDiscussPostCommentCountById(@Param("id") Integer id,
                                              @Param("commentCount") Integer commentCount);

    /**
     * 修改帖子类型
     * 0-普通
     * 1-置顶
     *
     * @param id
     * @param type
     * @return
     */
    int updateType(@Param("id") int id, @Param("type") int type);

    /**
     * 修改帖子状态
     * 0-正常
     * 1-精华
     * 2-拉黑
     *
     * @param id
     * @param status
     * @return
     */
    int updateStatus(@Param("id") int id, @Param("status") int status);

    /**
     * 修改帖子分数
     *
     * @param id
     * @param score
     * @return
     */
    int updateScore(@Param("id") int id, @Param("score") double score);
}
