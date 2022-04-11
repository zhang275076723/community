package com.zhang.java.service.Impl;

import com.zhang.java.domain.DiscussPost;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.service.DiscussPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Date 2022/4/3 20:16
 * @Author zsy
 * @Description
 */
@Service
public class DiscussPostMapperService implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId) {
        return discussPostMapper.selectDiscussPosts(userId);
    }

    @Override
    public Integer findDiscussPostRows(Integer userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

}
