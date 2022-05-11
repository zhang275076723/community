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
     * 当天的独立访客(unique visitor)数量，通过ip统计，使用HyperLogLog存储
     */
    private static final String PREFIX_UV = "uv";

    /**
     * 日活跃用户(daily active user)数量，通过用户id统计，使用Bitmap存储
     */
    private static final String PREFIX_DAU = "dau";

    /**
     * 帖子分数，在帖子点赞、帖子评论、加精时帖子分数会改变
     */
    private static final String PREFIX_POST = "post";

    /**
     * 某个实体的赞，包括帖子实体、帖子的评论实体、用户实体
     * like:entity:entityType:entityId -> set类型(userId)
     *
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户的赞，包括用户帖子的赞、用户评论的赞
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

    /**
     * 单日UV独立访客
     * uv:date -> HyperLogLog类型(访问的ip)
     * date格式：20220510
     *
     * @param date
     * @return
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 区间UV，合并单日UV得到区间UV
     * uv:startDate:endDate -> HyperLogLog类型(访问的ip)
     * date格式：20220510
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 单日活跃用户
     * dau:date -> Bitmap类型(userId表示的值对应的索引位置设置为true)
     * date格式：20220510
     *
     * @param date
     * @return
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 区间活跃用户
     * dau:startDate:endDate -> Bitmap类型(userId表示的值对应的索引位置设置为true)
     * date格式：20220510
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 帖子分数
     * post:score -> set类型(帖子id)
     * 将需要修改分数帖子放到set中，可以去重
     *
     * @return
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
