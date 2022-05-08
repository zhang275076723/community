package com.zhang.java.controller;

import com.zhang.java.annotation.LoginRequired;
import com.zhang.java.domain.User;
import com.zhang.java.service.FollowService;
import com.zhang.java.service.LikeService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @Date 2022/4/13 11:42
 * @Author zsy
 * @Description
 */
@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${server.servlet.context-path}")
    //项目路径名
    private String contextPath;

    @Value("${community.path.domain}")
    //服务器域名
    private String domain;

    @Value("${community.path.upload}")
    //上传路径
    private String uploadPath;

    /**
     * 用户账号设置页面
     *
     * @return
     */
    //使用自定义注解实现请求拦截，使用spring security拦截
//    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 用户上传头像
     *
     * @param file
     * @param model
     * @return
     */
    //使用自定义注解实现请求拦截，使用spring security拦截
//    @LoginRequired
    @PostMapping("/uploadHeaderImage")
    public String uploadHeaderImage(@RequestParam("headerImage") MultipartFile file, Model model) {
        if (file == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        //获取文件名
        String filename = file.getOriginalFilename();
        //文件后缀名
        String suffix = filename.substring(filename.lastIndexOf('.'));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            return "/site/setting";
        }

        //创建上传文件路径
        File uploadDirectory = new File(uploadPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdir();
        }
        //生成文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //上传文件路径
        File dest = new File(uploadPath + File.separator + filename);
        try {
            //文件上传
            file.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        // 更新用户头像
        User user = hostHolder.getUser();
        // localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     * 根据headerUrl访问本地存放的用户头像
     *
     * @param filename
     * @param response
     */
    @GetMapping("/header/{headerUrl}")
    public void getHeaderImage(@PathVariable("headerUrl") String filename, HttpServletResponse response) {
        String suffix = filename.substring(filename.lastIndexOf('.'));
        filename = uploadPath + File.separator + filename;
        //设置响应为图片类型
        response.setContentType("image/" + suffix);

        FileInputStream fis = null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(filename);
            os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = fis.read(buffer)) != -1) {
                os.write(buffer, 0, size);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 修改用户密码
     *
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @param model
     * @return
     */
    //使用自定义注解实现请求拦截，使用spring security拦截
//    @LoginRequired
    @GetMapping("/updatePassword")
    public String updatePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model) {

        User user = hostHolder.getUser();

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordMsg", "新密码和确认密码不一致！");
            return "/site/setting";
        }

        oldPassword = CommunityUtil.encodeMD5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "原密码错误！");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(), newPassword);

        return "redirect:/index";
    }

    /**
     * 用户个人主页
     *
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String profile(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean isFollowed = false;
        if (hostHolder.getUser() != null) {
            isFollowed = followService.isFollowed(hostHolder.getUser().getId(),
                    CommunityConstant.ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("isFollowed", isFollowed);

        return "/site/profile";
    }
}
