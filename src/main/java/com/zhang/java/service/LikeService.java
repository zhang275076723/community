package com.zhang.java.service;

/**
 * @Date 2022/4/23 17:22
 * @Author zsy
 * @Description
 */
public interface LikeService {
    /**
     * 对帖子或帖子评论或帖子评论的评论点赞或取消点赞
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    void like(int userId, int entityType, int entityId);

    /**
     * 查询帖子或帖子评论或帖子评论的评论的点赞数量
     *
     * @param entityType
     * @param entityId
     * @return
     */
    long findEntityLikeCount(int entityType, int entityId);

    /**
     * 查询用户对帖子或帖子评论或帖子评论的评论的点赞状态
     * 0-未点赞，1-点赞，-1-点踩
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    int findEntityLikeStatus(int userId, int entityType, int entityId);
}
