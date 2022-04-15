package com.zhang.java.controller;

import com.zhang.java.annotation.LoginRequired;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.User;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @Date 2022/4/15 15:56
 * @Author zsy
 * @Description
 */
@Controller
@RequestMapping("/discusspost")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/add")
    @ResponseBody
    @LoginRequired
    public String addDiscussPost(@RequestBody DiscussPost discussPost) {
        //ajax发送的是json数据，所以使用@RequestBody接收；如果发送js对象，使用@RequestParam接收

        //判断用户是否登录
        User user = hostHolder.getUser();
        if (user == null) {
            //403表示没有权限
            return CommunityUtil.getJSONString(403, "你还没有登录哦！", null);
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(discussPost.getTitle());
        post.setContent(discussPost.getContent());
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0, "发布成功！", null);
    }
}
