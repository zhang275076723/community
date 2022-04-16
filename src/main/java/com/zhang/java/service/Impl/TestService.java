package com.zhang.java.service.Impl;

import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.User;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.mapper.UserMapper;
import com.zhang.java.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Date 2022/4/16 21:21
 * @Author zsy
 * @Description
 */
@Service
public class TestService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * REQUIRED: 支持当前事务(外部事务),如果不存在则创建新事务.
     * REQUIRES_NEW: 创建一个新事务,并且暂停当前事务(外部事务).
     * NESTED: 如果当前存在事务(外部事务),则嵌套在该事务中执行(独立的提交和回滚),否则就会REQUIRED一样
     *
     * @return
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public Object save() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.encodeMD5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        //产生错误，事务回滚
        Integer.valueOf("abc");

        return "ok";
    }

}
