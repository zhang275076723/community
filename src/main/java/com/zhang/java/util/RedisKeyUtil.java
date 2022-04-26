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
}
