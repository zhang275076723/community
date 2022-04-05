package com.zhang.java.service;

import com.zhang.java.domain.DiscussPost;

import java.util.List;

/**
 * @Date 2022/4/3 20:15
 * @Author zsy
 * @Description
 */
public interface DiscussPostService {
    List<DiscussPost> findDiscussPosts(int userId);

    int findDiscussPostRows(int userId);
}
