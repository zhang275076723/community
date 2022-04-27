package com.zhang.java.service.impl;

import com.zhang.java.domain.User;
import com.zhang.java.service.FollowService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Date 2022/4/26 21:03
 * @Author zsy
 * @Description
 */
@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

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

    @Override
    public List<Map<String, Object>> findFolloweesWithFollowTime(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, CommunityConstant.ENTITY_TYPE_USER);
        //查询用户关注的用户，虽然是set集合无序的，但是redis返回自定义的set，是有序的
        Set<Integer> followeeIds = redisTemplate.opsForZSet().reverseRange(followeeKey,
                offset, offset + limit - 1);

        if (followeeIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer followeeId : followeeIds) {
            Map<String, Object> map = new HashMap<>();
            //关注的用户
            User followeeUser = userService.findUserById(followeeId);
            map.put("followeeUser", followeeUser);
            //关注的时间
            Double followTime = redisTemplate.opsForZSet().score(followeeKey, followeeId);
            map.put("followTime", new Date(followTime.longValue()));
            list.add(map);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> findFollowersWithFollowTime(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(CommunityConstant.ENTITY_TYPE_USER, userId);
        //查询该用户的粉丝，虽然是set集合无序的，但是redis返回自定义的set，是有序的
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey,
                offset, offset + limit - 1);

        if (followerIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer followerId : followerIds) {
            Map<String, Object> map = new HashMap<>();
            //该用户的粉丝
            User followerUser = userService.findUserById(followerId);
            map.put("followerUser", followerUser);
            //粉丝关注该用户的时间
            Double followTime = redisTemplate.opsForZSet().score(followerKey, followerId);
            map.put("followTime", new Date(followTime.longValue()));
            list.add(map);
        }
        return list;
    }
}
