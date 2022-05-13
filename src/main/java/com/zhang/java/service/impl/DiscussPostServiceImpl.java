package com.zhang.java.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Date 2022/4/3 20:16
 * @Author zsy
 * @Description
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    private Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 帖子热帖缓存的最大大小
     */
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    /**
     * 帖子热帖缓存的过期时间，单位s
     */
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    /**
     * 帖子热帖列表缓存
     */
    private LoadingCache<String, List<DiscussPost>> postListCache;

    /**
     * 帖子总数缓存
     */
    private LoadingCache<Integer, Integer> postRowsCache;

    /**
     * ioc容器实例化当前bean时，执行该方法
     * 初始化帖子热帖列表缓存和帖子热帖总数缓存，到本地缓存caffeine(内存中)
     */
    @PostConstruct
    public void init() {
        //初始化热帖帖子列表缓存，到本地缓存caffeine(内存中)
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        //当postListCache调用get()时才执行此方法

                        if (key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);
                        //如果本地缓存caffeine未命中，则查询二级缓存redis(未实现)，
                        //如果两级缓存都未命中，则查询数据库
                        logger.debug("load post list from DB.");
                        //caffeine将从数据库中查询到的热帖存放到postListCache(本地缓存，内存中)
                        return discussPostMapper.selectDiscussPosts(0, 1, offset, limit);
                    }
                });

        //初始化帖子列表缓存，到本地缓存caffeine(内存中)
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        //当postRowsCache调用get()时才执行此方法
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    /**
     * 根据查询模式查询帖子，只有查询热帖列表时才查询缓存
     * 优化：先查询本地缓存caffeine，如果缓存未命中，则查询二级缓存redis(未实现)，如果两级缓存都未命中，则查询数据库
     *
     * @param userId    用户id 0-查询所有帖子
     * @param orderMode 排序模式，0-正常排序，1-按帖子分数由高到低排序
     * @param limit     查询偏移量
     * @param offset    查询数量
     * @return
     */
    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId, int orderMode, int limit, int offset) {
        //只有查询所有热帖列表时才查询缓存
        if (userId == 0 && orderMode == 1) {
            //查询postListCache缓存中指定的key，key=limit:offset，如果没有会从CacheLoader中从内存中读取
            return postListCache.get(limit + ":" + offset);
        }

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPosts(userId, orderMode, limit, offset);
    }

    /**
     * 查询用户对应的帖子数量，只有时查询全部帖子数量时才查询缓存
     * 优化：先查询本地缓存caffeine，如果缓存未命中，则查询二级缓存redis(未实现)，如果两级缓存都未命中，则查询数据库
     *
     * @param userId 用户id 0-查询所有帖子
     * @return
     */
    @Override
    public Integer findDiscussPostRows(Integer userId) {
        //只有时查询全部帖子数量时才查询缓存
        if (userId == 0) {
            //查询postRowsCache缓存中指定的key，key=userId，如果没有会从CacheLoader中从内存中读取
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Override
    public Integer addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        discussPost.setType(0);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setScore(0.0);

        //转义帖子中的html标记，防止被浏览器解析
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //过滤帖子中的敏感词
        discussPost.setTitle(sensitiveFilter.filterSensitiveWords(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filterSensitiveWords(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost findDiscussPostById(Integer id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public Integer updateDiscussPostCommentCountById(Integer id, Integer commentCount) {
        return discussPostMapper.updateDiscussPostCommentCountById(id, commentCount);
    }

    @Override
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    @Override
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    @Override
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
