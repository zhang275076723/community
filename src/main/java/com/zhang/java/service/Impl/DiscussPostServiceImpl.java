package com.zhang.java.service.Impl;

import com.zhang.java.domain.DiscussPost;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
import java.util.List;

/**
 * @Date 2022/4/3 20:16
 * @Author zsy
 * @Description
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId) {
        return discussPostMapper.selectDiscussPosts(userId);
    }

    @Override
    public Integer findDiscussPostRows(Integer userId) {
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
}
