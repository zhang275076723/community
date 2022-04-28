package com.zhang.java.util;

/**
 * @Date 2022/4/23 17:15
 * @Author zsy
 * @Description
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_LOGIN_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    /**
     * 某个实体的赞
     * like:entity:entityType:entityId -> set类型(userId)
     *
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }


    /**
     * 某个用户的赞
     * like:user:userId -> string类型(用户点赞数量)
     *
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体
     * followee:userId:entityType -> zset(entityId, 关注的时间now)
     *
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的粉丝
     * follower:entityType:entityId -> zset(userId, 粉丝关注的时间now)
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 登录验证码
     * kaptcha:kaptchaOwner -> string类型(验证码)
     *
     * @param kaptchaOwner 验证码临时凭证
     * @return
     */
    public static String getKaptchaKey(String kaptchaOwner) {
        return PREFIX_KAPTCHA + SPLIT + kaptchaOwner;
    }

    /**
     * 用户登录凭证
     * ticket:ticket -> string类型(redis会自动将loginTicket对象序列化为json格式的字符串)
     *
     * @param ticket
     * @return
     */
    public static String getLoginTicketKey(String ticket) {
        return PREFIX_LOGIN_TICKET + SPLIT + ticket;
    }

    /**
     * 用户
     * user:userId -> string类型(redis会自动将user对象序列化为json格式的字符串)
     *
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
