package com.zhang.java.controller;

import com.zhang.java.domain.User;
import com.zhang.java.service.FollowService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Date 2022/4/26 21:17
 * @Author zsy
 * @Description
 */
@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(@RequestParam("entityType") int entityType,
                         @RequestParam("entityId") int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注！", null);
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(@RequestParam("entityType") int entityType,
                           @RequestParam("entityId") int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注！", null);
    }
}
