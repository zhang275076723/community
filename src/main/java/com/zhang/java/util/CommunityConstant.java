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
    public static int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    public static int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    public static int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的用户登录凭证超时时间，12小时
     */
    public static int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的用户登录凭证超时时间，100天
     */
    public static int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型，1-帖子的评论
     */
    public static int ENTITY_TYPE_DISCUSSPOST = 1;

    /**
     * 实体类型，2-帖子评论的评论或帖子评论的回复
     */
    public static int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型，3-用户
     */
    public static int ENTITY_TYPE_USER = 3;
}
