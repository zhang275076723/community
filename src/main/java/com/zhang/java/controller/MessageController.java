package com.zhang.java.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.Message;
import com.zhang.java.domain.User;
import com.zhang.java.service.MessageService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @Date 2022/4/17 17:10
 * @Author zsy
 * @Description
 */
@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 朋友私信界面，每个私信列表只显示最新的一条私信
     *
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping("/letter/list")
    public String getLetterList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                Model model) {
        User user = hostHolder.getUser();

        //私信列表分页
        PageHelper.startPage(pageNum, 5);
        //私信列表
        List<Message> conversations = messageService.findConversations(user.getId());
        PageInfo<Message> pageInfo = new PageInfo<>(conversations, 5);
        model.addAttribute("pageInfo", pageInfo);

        //私信列表、私信列表中私信数量、私信列表中未读私信数量、私信对方用户对应
        List<Map<String, Object>> conversionList = new ArrayList<>();
        for (Message message : conversations) {
            Map<String, Object> map = new HashMap<>();
            //私信列表
            map.put("message", message);
            //私信列表中私信数量
            map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
            //私信列表中未读私信数量
            map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
            //私信对方用户
            if (message.getToId() == user.getId()) {
                map.put("targetUser", userService.findUserById(message.getFromId()));
            } else {
                map.put("targetUser", userService.findUserById(message.getToId()));
            }
            conversionList.add(map);
        }
        model.addAttribute("conversionList", conversionList);

        //朋友私信未读数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //系统通知未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    /**
     * 私信列表详情页面，查询私信列表中的全部私信详情，并将此私信列表中的私信全部设置为已读
     *
     * @param pageNum
     * @param conversationId
     * @param model
     * @return
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  @PathVariable("conversationId") String conversationId,
                                  Model model) {
        User user = hostHolder.getUser();

        //私信列表中私信分页
        PageHelper.startPage(pageNum, 5);
        //私信列表中私信
        List<Message> letters = messageService.findLetters(conversationId);
        PageInfo<Message> pageInfo = new PageInfo<>(letters, 5);
        model.addAttribute("pageInfo", pageInfo);

        //私信列表中私信、发送私信用户对应
        List<Map<String, Object>> letterList = new ArrayList<>();
        for (Message letter : letters) {
            Map<String, Object> map = new HashMap<>();
            map.put("letter", letter);
            map.put("formUser", userService.findUserById(letter.getFromId()));
            letterList.add(map);
        }
        model.addAttribute("letterList", letterList);

        //当前用户所私信的目标用户
        String[] strId = conversationId.split("_");
        //Integer需要用equals比较是否相等
        if (user.getId().equals(Integer.parseInt(strId[0]))) {
            model.addAttribute("targetUser", userService.findUserById(Integer.parseInt(strId[1])));
        } else {
            model.addAttribute("targetUser", userService.findUserById(Integer.parseInt(strId[0])));
        }

        //model添加私信双方，用于分页跳转
        model.addAttribute("conversationId", conversationId);

        //将私信列表中的全部私信设置为已读
        List<Integer> ids = new ArrayList<>();
        for (Message message : letters) {
            if (message.getStatus() == 0 && message.getToId() == user.getId()) {
                ids.add(message.getId());
            }
        }
        if (!ids.isEmpty()) {
            messageService.readUnReadMessage(ids);
        }

        return "/site/letter-detail";
    }

    /**
     * 发送私信
     *
     * @param toName
     * @param content
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(@RequestParam("toName") String toName,
                             @RequestParam("content") String content) {
        User user = hostHolder.getUser();

        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "私信对方用户不存在！", null);
        }

        Message message = new Message();
        message.setFromId(user.getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0, "私信发送成功！", null);
    }

    /**
     * 系统通知页面
     * 包含3类系统通知：
     * 1、评论通知
     * 2、点赞通知
     * 3、关注通知
     *
     * @param model
     * @return
     */
    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        //评论通知
        //最新一条系统评论通知
        Message lastestCommentNotice = messageService.findLastestNotice(user.getId(),
                CommunityConstant.TOPIC_COMMENT);
        if (lastestCommentNotice != null) {
            //当系统评论通知不为空的情况下实例化此map
            Map<String, Object> commentNoticeMap = new HashMap<>();

            commentNoticeMap.put("lastestCommentNotice", lastestCommentNotice);

            //{&quot;discussPostId&quot;:281,
            // &quot;entityType&quot;:2,
            // &quot;entityId&quot;:237,
            // &quot;userId&quot;:115}
            //将评论通知中的转义字符内容反转义
            String content = HtmlUtils.htmlUnescape(lastestCommentNotice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, Map.class);
            commentNoticeMap.put("discussPostId", data.get("discussPostId"));
            commentNoticeMap.put("entityType", data.get("entityType"));
            commentNoticeMap.put("entityId", data.get("entityId"));
            commentNoticeMap.put("user", userService.findUserById((Integer) data.get("userId")));

            //系统评论通知数量
            int commentNoticeCount = messageService.findNoticeCount(user.getId(),
                    CommunityConstant.TOPIC_COMMENT);
            commentNoticeMap.put("commentNoticeCount", commentNoticeCount);

            //系统评论通知未读数量
            int commentNoticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),
                    CommunityConstant.TOPIC_COMMENT);
            commentNoticeMap.put("commentNoticeUnreadCount", commentNoticeUnreadCount);

            model.addAttribute("commentNoticeMap", commentNoticeMap);
        }

        //点赞通知
        //最新一条系统点赞通知
        Message lastestLikeNotice = messageService.findLastestNotice(user.getId(),
                CommunityConstant.TOPIC_LIKE);
        if (lastestLikeNotice != null) {
            //当系统点赞通知不为空的情况下实例化此map
            Map<String, Object> likeNoticeMap = new HashMap<>();

            likeNoticeMap.put("lastestLikeNotice", lastestLikeNotice);

            //{&quot;discussPostId&quot;:281,
            // &quot;entityType&quot;:2,
            // &quot;entityId&quot;:237,
            // &quot;userId&quot;:115}
            //将点赞通知中的转义字符内容反转义
            String content = HtmlUtils.htmlUnescape(lastestLikeNotice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, Map.class);
            likeNoticeMap.put("discussPostId", data.get("discussPostId"));
            likeNoticeMap.put("entityType", data.get("entityType"));
            likeNoticeMap.put("entityId", data.get("entityId"));
            likeNoticeMap.put("user", userService.findUserById((Integer) data.get("userId")));

            //系统点赞通知数量
            int likeNoticeCount = messageService.findNoticeCount(user.getId(),
                    CommunityConstant.TOPIC_LIKE);
            likeNoticeMap.put("likeNoticeCount", likeNoticeCount);

            //系统点赞通知未读数量
            int likeNoticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),
                    CommunityConstant.TOPIC_LIKE);
            likeNoticeMap.put("likeNoticeUnreadCount", likeNoticeUnreadCount);

            model.addAttribute("likeNoticeMap", likeNoticeMap);
        }

        //关注通知
        //最新一条系统关注通知
        Message lastestFollowNotice = messageService.findLastestNotice(user.getId(),
                CommunityConstant.TOPIC_FOLLOW);
        if (lastestFollowNotice != null) {
            //当系统关注通知不为空的情况下实例化此map
            Map<String, Object> followNoticeMap = new HashMap<>();

            followNoticeMap.put("lastestFollowNotice", lastestFollowNotice);

            //{&quot;entityType&quot;:2,
            // &quot;entityId&quot;:237,
            // &quot;userId&quot;:115}
            //将关注通知中的转义字符内容反转义
            String content = HtmlUtils.htmlUnescape(lastestFollowNotice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, Map.class);
            followNoticeMap.put("entityType", data.get("entityType"));
            followNoticeMap.put("entityId", data.get("entityId"));
            followNoticeMap.put("user", userService.findUserById((Integer) data.get("userId")));

            //系统关注通知数量
            int followNoticeCount = messageService.findNoticeCount(user.getId(),
                    CommunityConstant.TOPIC_FOLLOW);
            followNoticeMap.put("followNoticeCount", followNoticeCount);

            //系统关注通知未读数量
            int followNoticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),
                    CommunityConstant.TOPIC_FOLLOW);
            followNoticeMap.put("followNoticeUnreadCount", followNoticeUnreadCount);

            model.addAttribute("followNoticeMap", followNoticeMap);
        }

        //朋友私信未读数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //系统通知未读数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    /**
     * 系统通知详情页面，查询某个主题的全部系统通知，并将此主题的系统通知全部设置为已读
     *
     * @param pageNum
     * @param topic
     * @param model
     * @return
     */
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @PathVariable("topic") String topic,
                                  Model model) {
        User user = hostHolder.getUser();

        //系统通知分页
        PageHelper.startPage(pageNum, 5);
        //当前主题的全部系统通知
        List<Message> notices = messageService.findNotices(user.getId(), topic);
        PageInfo<Message> pageInfo = new PageInfo<>(notices, 3);
        model.addAttribute("pageInfo", pageInfo);

        //当前主题的系统通知列表
        List<Map<String, Object>> noticeList = new ArrayList<>();
        for (Message notice : notices) {
            Map<String, Object> map = new HashMap<>();
            //系统通知
            map.put("notice", notice);
            //内容
            //反转义html标记
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, Map.class);
            map.put("user", userService.findUserById((Integer) data.get("userId")));
            map.put("entityType", data.get("entityType"));
            map.put("entityId", data.get("entityId"));
            //只有评论、点赞系统通知需要discussPostId；关注系统通知不需要，则为null
            map.put("discussPostId", data.get("discussPostId"));
            //系统通知作者为系统用户，id为1
            map.put("fromUser", userService.findUserById(notice.getFromId()));

            noticeList.add(map);
        }
        model.addAttribute("noticeList", noticeList);

        //添加系统通知的主题，用于不同的主题有不同的通知和分页跳转
        model.addAttribute("topic", topic);

        //将此主题的系统通知全部设置为已读
        List<Integer> ids = new ArrayList<>();
        for (Message notice : notices) {
            if (notice.getStatus() == 0 && notice.getToId() == user.getId()) {
                ids.add(notice.getId());
            }
        }
        if (!ids.isEmpty()) {
            messageService.readUnReadMessage(ids);
        }

        return "/site/notice-detail";
    }
}
