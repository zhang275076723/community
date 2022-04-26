package com.zhang.java.service;

/**
 * @Date 2022/4/26 21:03
 * @Author zsy
 * @Description
 */
public interface FollowService {
    /**
     * 用户关注某一实体，可能为用户、帖子、帖子评论、帖子评论的评论、帖子评论的回复
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    void follow(int userId, int entityType, int entityId);

    /**
     * 用户取消关注某一实体，可能为用户、帖子、帖子评论、帖子评论的评论、帖子评论的回复
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    void unfollow(int userId, int entityType, int entityId);

    /**
     * 用户关注某一实体的数量
     *
     * @param userId
     * @param entityType
     * @return
     */
    long findFolloweeCount(int userId, int entityType);

    /**
     * 某一实体的粉丝数量
     *
     * @param userId
     * @param entityType
     * @return
     */
    long findFollowerCount(int entityType, int entityId);

    /**
     * 用户是否关注某一实体
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    boolean isFollowed(int userId, int entityType, int entityId);
}
