package com.zhang.java.service;

import com.zhang.java.domain.DiscussPost;

import java.util.List;

/**
 * @Date 2022/4/3 20:15
 * @Author zsy
 * @Description
 */
public interface DiscussPostService {
    List<DiscussPost> findDiscussPosts(Integer userId, int orderMode, int limit, int offset);

    Integer findDiscussPostRows(Integer userId);

    Integer addDiscussPost(DiscussPost discussPost);

    DiscussPost findDiscussPostById(Integer id);

    Integer updateDiscussPostCommentCountById(Integer id, Integer commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);
}
