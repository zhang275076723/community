package com.zhang.java.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.User;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Date 2022/4/3 20:30
 * @Author zsy
 * @Description
 */
@Controller
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping("/")
    public String homePage(){
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String getIndexPage(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               Model model) {
        //帖子分页
        PageHelper.startPage(pageNum, 10);
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0);
        PageInfo<DiscussPost> pageInfo = new PageInfo<>(discussPosts, 5);
        model.addAttribute("pageInfo", pageInfo);

        //存放用户和帖子信息
        List<Map<String,Object>> discussPostAndUserList = new ArrayList<>();
        for (DiscussPost discussPost : discussPosts) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(discussPost.getUserId());
            map.put("user", user);
            map.put("discussPost", discussPost);
            discussPostAndUserList.add(map);
        }
        model.addAttribute("discussPostAndUserList", discussPostAndUserList);

        return "index";
    }
}
