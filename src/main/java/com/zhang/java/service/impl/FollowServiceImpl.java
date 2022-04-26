package com.zhang.java.service.impl;

import com.zhang.java.service.FollowService;
import com.zhang.java.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @Date 2022/4/26 21:03
 * @Author zsy
 * @Description
 */
@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void follow(int userId, int entityType, int entityId) {
        //因为要保证用户关注的实体和实体拥有的粉丝两者一致性，所以要使用事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                //开启事务
                operations.multi();
                //添加用户关注的实体
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                //添加实体拥有的粉丝
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                //提交事务
                return operations.exec();
            }
        });
    }

    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        //因为要保证用户关注的实体和实体拥有的粉丝两者一致性，所以要使用事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                //开启事务
                operations.multi();
                //删除用户关注的实体
                operations.opsForZSet().remove(followeeKey, entityId);
                //删除实体拥有的粉丝
                operations.opsForZSet().remove(followerKey, userId);
                //提交事务
                return operations.exec();
            }
        });
    }

    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    @Override
    public boolean isFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }
}
