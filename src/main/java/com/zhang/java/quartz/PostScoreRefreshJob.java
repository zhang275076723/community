package com.zhang.java.quartz;

import com.zhang.java.domain.DiscussPost;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.ElasticsearchService;
import com.zhang.java.service.LikeService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Date 2022/5/11 19:35
 * @Author zsy
 * @Description 帖子分数刷新任务，将redis中存储的帖子进行帖子分数刷新，并将更新分数后的帖子存放到es中
 */
public class PostScoreRefreshJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    /**
     * 社区纪元
     */
    private static final Date epoch;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    //只需要初始化一次
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-04-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        //redis中没有要刷新的帖子
        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数，需要刷新帖子数量: " + operations.size());
        while (operations.size() > 0) {
            //每次随机弹出一个帖子id，进行刷新
            refreshPost((Integer) operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    /**
     * 根据id刷新帖子
     *
     * @param postId
     */
    private void refreshPost(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        //要刷新的帖子被管理员删除，则不需要进行刷新
        if (post == null) {
            logger.error("该帖子不存在: postId = " + postId);
            return;
        }

        //是否加精
        boolean wonderful = post.getStatus() == 1;
        //帖子评论数量，不包括帖子评论的评论数量
        int commentCount = post.getCommentCount();
        //帖子点赞数量，不包括帖子评论的点赞数数量、帖子评论的评论点赞数量、帖子评论的回复点赞数量
        long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_DISCUSSPOST, postId);

        //帖子权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //帖子分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1)) //避免取log值为负数
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        //更新帖子分数
        discussPostService.updateScore(postId, score);

        //将更新分数后的帖子存放到es中
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
