package com.zhang.java.service;

import com.zhang.java.domain.DiscussPost;

import java.util.List;

/**
 * @Date 2022/4/3 20:15
 * @Author zsy
 * @Description
 */
public interface DiscussPostService {
    List<DiscussPost> findDiscussPosts(Integer userId);

    Integer findDiscussPostRows(Integer userId);

    Integer addDiscussPost(DiscussPost discussPost);
}
