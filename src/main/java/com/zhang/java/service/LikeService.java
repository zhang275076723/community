package com.zhang.java.service;

/**
 * @Date 2022/4/23 17:22
 * @Author zsy
 * @Description
 */
public interface LikeService {
    /**
     * 用户对帖子、帖子评论、帖子评论的评论点赞或取消点赞
     * 保证帖子实体或帖子评论实体或帖子评论的评论实体和用户实体赞所对应，保持一致性
     *
     * @param userId       当前登录的用户
     * @param entityType   实体类型
     * @param entityId     实体id
     * @param entityUserId 实体用户，即当前实体是由哪个用户所创建的
     */
    void like(int userId, int entityType, int entityId, int entityUserId);

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

    /**
     *
     * @param userId
     * @return
     */
    int findUserLikeCount(int userId);
}
