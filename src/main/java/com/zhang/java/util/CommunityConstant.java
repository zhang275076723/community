package com.zhang.java.util;

/**
 * @Date 2022/4/10 16:47
 * @Author zsy
 * @Description
 */
public class CommunityConstant {
    /**
     * 激活成功
     */
    public static final int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    public static final int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    public static final int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的用户登录凭证超时时间，12小时
     */
    public static final int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的用户登录凭证超时时间，100天
     */
    public static final int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型，1-帖子，帖子评论的实体类型为1-帖子
     */
    public static final int ENTITY_TYPE_DISCUSSPOST = 1;

    /**
     * 实体类型，2-帖子的评论，帖子评论的评论或帖子评论的回复的实体类型为2-帖子的评论
     */
    public static final int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型，3-用户
     */
    public static final int ENTITY_TYPE_USER = 3;

    /**
     * 主题，评论
     */
    public static final String TOPIC_COMMENT = "comment";

    /**
     * 主题，点赞
     */
    public static final String TOPIC_LIKE = "like";

    /**
     * 主题，关注
     */
    public static final String TOPIC_FOLLOW = "follow";

    /**
     * 主题，发帖、置顶帖子、帖子加精
     */
    public static final String TOPIC_PUBLISH = "publish";

    /**
     * 主题，删帖
     */
    public static final String TOPIC_DELETE = "delete";

    /**
     * 系统用户id
     */
    public static final int SYSTEM_USER_ID = 1;

    /**
     * 权限: 普通用户
     */
    public static final String AUTHORITY_USER = "user";

    /**
     * 权限: 管理员
     * 帖子删除
     */
    public static final String AUTHORITY_ADMIN = "admin";

    /**
     * 权限: 版主
     * 帖子置顶、加精
     */
    public static final String AUTHORITY_MODERATOR = "moderator";
}
