package com.zhang.java.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.Message;
import com.zhang.java.domain.User;
import com.zhang.java.service.MessageService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Date 2022/4/17 17:10
 * @Author zsy
 * @Description
 */
@Controller
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //查询私信列表
    @GetMapping("/list")
    public String getConversationList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
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

        //用户未读私信数量
        model.addAttribute("userUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));

        return "/site/letter";
    }

    //点击私信列表，查询私信列表中的全部私信详情
    @GetMapping("/letter/list/{conversationId}")
    public String getLetterList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                @PathVariable("conversationId") String conversationId, Model model) {
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
        if (hostHolder.getUser().getId().equals(Integer.parseInt(strId[0]))) {
            model.addAttribute("targetUser", userService.findUserById(Integer.parseInt(strId[1])));
        } else {
            model.addAttribute("targetUser", userService.findUserById(Integer.parseInt(strId[0])));
        }

        //model添加私信双方，用于分页跳转
        model.addAttribute("conversationId", conversationId);

        //将私信列表中的全部私信设置为已读
        List<Integer> ids = new ArrayList<>();
        for (Message message : letters) {
            if (message.getStatus() == 0 && message.getToId() == hostHolder.getUser().getId()) {
                ids.add(message.getId());
            }
        }
        if (!ids.isEmpty()) {
            messageService.readUnReadMessage(ids);
        }

        return "/site/letter-detail";
    }

    @PostMapping("/send")
    @ResponseBody
    public String sendMessage(@RequestParam("toName") String toName,
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

}
