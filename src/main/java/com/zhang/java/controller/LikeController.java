package com.zhang.java.controller;

import com.zhang.java.annotation.LoginRequired;
import com.zhang.java.domain.Event;
import com.zhang.java.domain.User;
import com.zhang.java.event.EventProducer;
import com.zhang.java.service.LikeService;
import com.zhang.java.util.CommunityConstant;
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

    @Autowired
    private EventProducer eventProducer;

    /**
     * 用户点赞和取消点赞，保证实体赞和用户实体赞对应（一致性），用事务完成
     *
     * @param entityType    实体类型
     * @param entityId      实体id
     * @param entityUserId  实体用户id，即当前实体是由哪个用户所创建的
     * @param discussPostId 当前点赞所属帖子id
     * @return
     */
    @PostMapping("/like")
    @ResponseBody
    @LoginRequired
    public String like(int entityType, int entityId, int entityUserId, int discussPostId) {
        User user = hostHolder.getUser();
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //实体点赞数量
        long entityLikeCount = likeService.findEntityLikeCount(entityType, entityId);
        //实体点赞状态
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("entityLikeCount", entityLikeCount);
        map.put("entityLikeStatus", entityLikeStatus);

        //只有点赞时才触发点赞事件，取消点赞不触发点赞事件
        if (entityLikeStatus == 1) {
            //点赞事件，需要在命令行手动启动kafka、zookeeper
            Event event = new Event();
            event.setTopic(CommunityConstant.TOPIC_LIKE);
            event.setUserId(user.getId());
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(entityUserId);
            //因为点赞事件，点击可以跳转到对应帖子，所以需要帖子的id
            event.setData("discussPostId", discussPostId);
            //触发点赞事件
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
