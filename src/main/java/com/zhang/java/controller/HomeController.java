package com.zhang.java.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.User;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.LikeService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @Autowired
    private LikeService likeService;

    @RequestMapping("/")
    public String homePage() {
        return "redirect:/index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    @GetMapping("/index")
    public String getIndexPage(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               Model model) {
        //帖子分页
        PageHelper.startPage(pageNum, 10);
        //userId为0，表示查询全部帖子
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0);
        PageInfo<DiscussPost> pageInfo = new PageInfo<>(discussPosts, 5);
        model.addAttribute("pageInfo", pageInfo);

        //用户和帖子对应
        List<Map<String, Object>> discussPostAndUserList = new ArrayList<>();
        for (DiscussPost discussPost : discussPosts) {
            //存放对应关系
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(discussPost.getUserId());
            //用户
            map.put("user", user);
            //帖子
            map.put("discussPost", discussPost);
            //帖子的点赞数量
            map.put("discussPostLikeCount", likeService.findEntityLikeCount(
                    CommunityConstant.ENTITY_TYPE_DISCUSSPOST, discussPost.getId()));
            discussPostAndUserList.add(map);
        }
        model.addAttribute("discussPostAndUserList", discussPostAndUserList);

        return "index";
    }
}
