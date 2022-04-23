package com.zhang.java.controller;

import com.zhang.java.annotation.LoginRequired;
import com.zhang.java.domain.User;
import com.zhang.java.service.LikeService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @Date 2022/4/23 18:00
 * @Author zsy
 * @Description
 */
@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/like")
    @ResponseBody
    @LoginRequired
    public String like(int entityType, int entityId) {
        User user = hostHolder.getUser();
        likeService.like(user.getId(), entityType, entityId);
        long entityLikeCount = likeService.findEntityLikeCount(entityType, entityId);
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("entityLikeCount", entityLikeCount);
        map.put("entityLikeStatus", entityLikeStatus);
        return CommunityUtil.getJSONString(0, null, map);
    }
}
