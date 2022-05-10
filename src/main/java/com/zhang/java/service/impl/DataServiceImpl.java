package com.zhang.java.service.impl;

import com.zhang.java.service.DataService;
import com.zhang.java.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Date 2022/5/10 16:59
 * @Author zsy
 * @Description
 */
@Service
public class DataServiceImpl implements DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    @Override
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        //日期范围内的所有key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        //当前日期没有超过结束日期时，往keyList中添加key
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            //当前日期往后加一天
            calendar.add(Calendar.DATE, 1);
        }

        //合并这些key
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    @Override
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        //将当前用户userId对应的索引位置设置为true
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    @Override
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        //日期范围内的所有key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        //当前日期没有超过结束日期时，往keyList中添加key
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            //当前日期往后加一天
            calendar.add(Calendar.DATE, 1);
        }

        //通过OR运算，合并这些key
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
