package com.zhang.java.service.impl;

import com.zhang.java.service.LikeService;
import com.zhang.java.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @Date 2022/4/23 17:22
 * @Author zsy
 * @Description
 */
@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //因为要保证实体点赞和实体用户点赞数量两者一致性，所以要使用事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //查询要放在事务之外，否则不会生效
                Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

                //开启事务
                operations.multi();
                if (isMember) {
                    //实体取消点赞
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    //实体用户点赞数量减1，如果userLikeKey不存在会自动添加此key
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    //实体点赞
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    //实体用户点赞数量加1，如果userLikeKey不存在会自动添加此key
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                //提交事务
                return operations.exec();
            }
        });
    }

    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember) {
            //已点赞
            return 1;
        } else {
            //未点赞
            return 0;
        }
    }

    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}
