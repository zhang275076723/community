package com.zhang.java.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.Page;
import com.zhang.java.domain.User;
import com.zhang.java.service.FollowService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Date 2022/4/26 21:17
 * @Author zsy
 * @Description
 */
@Controller
public class FollowController {
    @Autowired
    private UserService userService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 关注某个实体
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("/follow")
    @ResponseBody
    public String follow(@RequestParam("entityType") int entityType,
                         @RequestParam("entityId") int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注！", null);
    }

    /**
     * 取关某个实体
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(@RequestParam("entityType") int entityType,
                           @RequestParam("entityId") int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注！", null);
    }

    /**
     * 用户关注的用户
     * 使用自定义用户分页，因为使用redis，不能使用PageHelper
     *
     * @param userId
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping("/followees/{userId}")
    public String followees(@PathVariable("userId") int userId,
                            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                            Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        //此用户
        model.addAttribute("user", user);

        //此用户关注的用户分页，使用自定义分页，不能使用PageHelper
        Page page = new Page();
        page.setPageSize(5);
        page.setTotalRows((int) followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));
        page.setTotalPages();
        page.setPageNum(pageNum);
        page.setUrlPath("/followees/" + userId);
        model.addAttribute("page", page);

        List<Map<String, Object>> followees = followService.findFolloweesWithFollowTime(userId,
                page.getOffset(), page.getPageSize());

        for (Map<String, Object> map : followees) {
            User followeeUser = (User) map.get("followeeUser");
            //当前用户是否关注了此用户关注的用户
            boolean isFollowed = false;
            if (hostHolder.getUser() != null) {
                isFollowed = followService.isFollowed(hostHolder.getUser().getId(),
                        CommunityConstant.ENTITY_TYPE_USER, followeeUser.getId());
            }
            //map中添加当前用户是否关注了此用户关注的用户
            map.put("isFollowed", isFollowed);
        }
        //此用户关注的用户
        model.addAttribute("followees", followees);

        return "site/followee";
    }

    /**
     * 用户的粉丝
     * 使用自定义用户分页，因为使用redis，不能使用PageHelper
     *
     * @param userId
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping("/followers/{userId}")
    public String followers(@PathVariable("userId") int userId,
                            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                            Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        //此用户
        model.addAttribute("user", user);

        //此用户的粉丝分页，使用自定义分页，不能使用PageHelper
        Page page = new Page();
        page.setPageSize(5);
        page.setTotalRows((int) followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId));
        page.setTotalPages();
        page.setPageNum(pageNum);
        page.setUrlPath("/followers/" + userId);
        model.addAttribute("page", page);

        List<Map<String, Object>> followers = followService.findFollowersWithFollowTime(userId,
                page.getOffset(), page.getPageSize());

        for (Map<String, Object> map : followers) {
            User followerUser = (User) map.get("followerUser");
            //当前用户是否关注了此用户的粉丝
            boolean isFollowed = false;
            if (hostHolder.getUser() != null) {
                isFollowed = followService.isFollowed(hostHolder.getUser().getId(),
                        CommunityConstant.ENTITY_TYPE_USER, followerUser.getId());
            }
            //map中添加当前用户是否关注了此用户的粉丝
            map.put("isFollowed", isFollowed);
        }
        //此用户的粉丝
        model.addAttribute("followers", followers);

        return "site/follower";
    }

}
