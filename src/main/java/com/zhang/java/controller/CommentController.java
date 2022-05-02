package com.zhang.java.controller;

import com.zhang.java.domain.Comment;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.Event;
import com.zhang.java.event.EventProducer;
import com.zhang.java.service.CommentService;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @Date 2022/4/16 22:08
 * @Author zsy
 * @Description
 */
@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 添加帖子评论
     *
     * @param discussPostId
     * @param comment
     * @return
     */
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") Integer discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //评论事件，需要在命令行手动启动kafka、zookeeper
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_COMMENT);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());
        //因为评论事件，点击可以跳转到对应帖子，所以需要帖子的id
        event.setData("discussPostId", discussPostId);
        //当前评论是帖子的评论
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_DISCUSSPOST) {
            DiscussPost targetDiscussPost = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(targetDiscussPost.getUserId());
        } else if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_COMMENT) {
            //当前评论是帖子的评论
            Comment targetComment = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(targetComment.getUserId());
        }
        //触发评论事件
        eventProducer.fireEvent(event);

        return "redirect:/discusspost/detail/" + discussPostId;
    }
}
