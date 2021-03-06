package com.zhang.java.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.annotation.LoginRequired;
import com.zhang.java.domain.Comment;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.Event;
import com.zhang.java.domain.User;
import com.zhang.java.event.EventProducer;
import com.zhang.java.service.CommentService;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.LikeService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import com.zhang.java.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Date 2022/4/15 15:56
 * @Author zsy
 * @Description
 */
@Controller
@RequestMapping("/discussPost")
public class DiscussPostController {
    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布帖子
     *
     * @param discussPost
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    //使用自定义注解实现请求拦截，使用spring security拦截
//    @LoginRequired
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
        discussPostService.addDiscussPost(post);

        //发帖事件，将帖子存放到es中，需要在命令行手动启动kafka、zookeeper
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(CommunityConstant.ENTITY_TYPE_DISCUSSPOST);
        event.setEntityId(post.getId());
        //触发发帖事件
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0, "发布成功！", null);
    }

    /**
     * 帖子详情界面
     *
     * @param discussPostId
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") Integer discussPostId,
                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                 Model model) {
        User user = hostHolder.getUser();

        //帖子和帖子发布用户信息
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        User discussPostUser = userService.findUserById(discussPost.getUserId());
        //将帖子和帖子发布用户放入model，前端获取
        model.addAttribute("discussPost", discussPost);
        model.addAttribute("user", discussPostUser);

        //帖子的点赞数量
        model.addAttribute("discussPostLikeCount", likeService.findEntityLikeCount(
                CommunityConstant.ENTITY_TYPE_DISCUSSPOST, discussPostId));
        //用户对帖子的点赞状态，0-未点赞，1-点赞，-1-点踩
        if (user == null) {
            model.addAttribute("discussPostLikeStatus", 0);
        } else {
            model.addAttribute("discussPostLikeStatus", likeService.findEntityLikeStatus(
                    user.getId(), CommunityConstant.ENTITY_TYPE_DISCUSSPOST, discussPostId));
        }

        //帖子评论分页
        PageHelper.startPage(pageNum, 5);
        //帖子评论
        List<Comment> commentList = commentService.findCommentsByEntity(
                CommunityConstant.ENTITY_TYPE_DISCUSSPOST, discussPost.getId());
        PageInfo<Comment> pageInfo = new PageInfo<>(commentList, 5);
        model.addAttribute("pageInfo", pageInfo);

        //帖子评论、帖子评论发布用户、帖子评论的评论数量、帖子评论的评论对应
        List<Map<String, Object>> commentsList = new ArrayList<>();
        for (Comment comment : commentList) {
            //帖子评论、帖子评论发布用户、帖子回复数量对应
            Map<String, Object> map = new HashMap<>();
            //帖子评论
            map.put("comment", comment);
            //帖子评论发布用户
            map.put("user", userService.findUserById(comment.getUserId()));
            //帖子评论的评论数量
            map.put("replyCount", commentService.findCommentsCount(
                    CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId()));

            //帖子评论的点赞数量
            map.put("commentLikeCount", likeService.findEntityLikeCount(
                    CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId()));
            //用户对帖子评论的点赞状态，0-未点赞，1-点赞，-1-点踩
            if (user == null) {
                map.put("commentLikeStatus", 0);
            } else {
                map.put("commentLikeStatus", likeService.findEntityLikeStatus(
                        user.getId(), CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId()));
            }

            //帖子评论的评论
            List<Comment> list = commentService.findCommentsByEntity(
                    CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());
            List<Map<String, Object>> comment2CommentList = new ArrayList<>();
            for (Comment comment2Comment : list) {
                //帖子评论的评论、帖子评论的评论用户、评论回复用户对应
                Map<String, Object> comment2CommentMap = new HashMap<>();
                //帖子评论的评论
                comment2CommentMap.put("comment2Comment", comment2Comment);
                //帖子评论的评论用户
                comment2CommentMap.put("user", userService.findUserById(comment2Comment.getUserId()));

                //帖子评论的评论的点赞数量
                comment2CommentMap.put("comment2CommentLikeCount", likeService.findEntityLikeCount(
                        CommunityConstant.ENTITY_TYPE_COMMENT, comment2Comment.getId()));
                //用户对帖子评论的评论的点赞状态，0-未点赞，1-点赞，-1-点踩
                if (user == null) {
                    comment2CommentMap.put("comment2CommentLikeStatus", 0);
                } else {
                    comment2CommentMap.put("comment2CommentLikeStatus", likeService.findEntityLikeStatus(
                            user.getId(), CommunityConstant.ENTITY_TYPE_COMMENT, comment2Comment.getId()));
                }

                //帖子评论的评论回复用户
                if (comment2Comment.getTargetId() == 0) {
                    //对帖子评论进行评论，不需要回复用户id
                    comment2CommentMap.put("targetUser", null);
                } else {
                    //对帖子评论进行回复，需要回复用户的id
                    User targetUser = userService.findUserById(comment2Comment.getTargetId());
                    comment2CommentMap.put("targetUser", targetUser);
                }
                comment2CommentList.add(comment2CommentMap);
            }

            //帖子评论的评论
            map.put("comment2CommentList", comment2CommentList);
            //帖子评论
            commentsList.add(map);
        }
        model.addAttribute("commentsList", commentsList);

        return "/site/discuss-detail";
    }

    /**
     * 置顶帖子和取消置顶帖子，即将帖子type设置为1或0
     *
     * @param discussPostId
     * @return
     */
    @PostMapping("/top")
    @ResponseBody
    public String setTop(@RequestParam("discussPostId") int discussPostId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        int type = discussPost.getType() == 1 ? 0 : 1;
        //置顶帖子或取消置顶帖子
        discussPostService.updateType(discussPostId, type);

        //置顶帖子和取消置顶帖子事件，将更新后的帖子存放到es中，需要在命令行手动启动kafka、zookeeper
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(CommunityConstant.ENTITY_TYPE_DISCUSSPOST);
        event.setEntityId(discussPostId);
        //触发置顶帖子和取消置顶帖子事件
        eventProducer.fireEvent(event);

        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        return CommunityUtil.getJSONString(0, null, map);
    }

    /**
     * 加精帖子和取消加精帖子，即将帖子status设置为1或0
     *
     * @param discussPostId
     * @return
     */
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(@RequestParam("discussPostId") int discussPostId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        int status = discussPost.getStatus() == 1 ? 0 : 1;
        //加精帖子或取消加精帖子
        discussPostService.updateStatus(discussPostId, status);

        //加精帖子和取消加精帖子事件，将更新后的帖子存放到es中，需要在命令行手动启动kafka、zookeeper
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(CommunityConstant.ENTITY_TYPE_DISCUSSPOST);
        event.setEntityId(discussPostId);
        //触发加精帖子和取消加精帖子事件
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPostId);

        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return CommunityUtil.getJSONString(0, null, map);
    }

    /**
     * 删除帖子，即将帖子status设置为2
     *
     * @param discussPostId
     * @return
     */
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(@RequestParam("discussPostId") int discussPostId) {
        discussPostService.updateStatus(discussPostId, 2);

        //帖子删除事件，将该帖子从es中删除，需要在命令行手动启动kafka、zookeeper
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_DELETE);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(CommunityConstant.ENTITY_TYPE_DISCUSSPOST);
        event.setEntityId(discussPostId);
        //触发帖子删除
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, null, null);
    }
}
