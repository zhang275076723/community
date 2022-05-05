package com.zhang.java.service;

import com.zhang.java.domain.DiscussPost;

import java.io.IOException;
import java.util.List;

/**
 * @Date 2022/5/5 16:54
 * @Author zsy
 * @Description
 */
public interface ElasticsearchService {
    /**
     * 保存帖子到es中
     *
     * @param discussPost
     */
    void saveDiscussPost(DiscussPost discussPost);

    /**
     * 根据帖子id删除es中的帖子
     *
     * @param id
     */
    void deleteDiscussPost(int id);

    /**
     * 根据关键字查询es中的帖子数量
     *
     * @param keyword
     * @return
     * @throws IOException
     */
    long searchDiscussPostCount(String keyword) throws IOException;

    /**
     * 根据关键字查询es中的帖子，对keyword进行高亮显示，支持分页
     *
     * @param keyword
     * @param offset
     * @param limit
     * @return
     * @throws IOException
     */
    List<DiscussPost> searchDiscussPost(String keyword, int offset, int limit) throws IOException;
}
