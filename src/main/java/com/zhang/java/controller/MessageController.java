package com.zhang.java.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.Message;
import com.zhang.java.domain.User;
import com.zhang.java.service.MessageService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //查询私信列表中私信详情
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
        String[] ids = conversationId.split("_");
        //Integer需要用equals比较是否相等
        if (hostHolder.getUser().getId().equals(Integer.parseInt(ids[0]))) {
            model.addAttribute("targetUser", userService.findUserById(Integer.parseInt(ids[1])));
        } else {
            model.addAttribute("targetUser", userService.findUserById(Integer.parseInt(ids[0])));
        }

        //私信双方，用于分页跳转
        model.addAttribute("conversationId", conversationId);

        return "/site/letter-detail";
    }

}
