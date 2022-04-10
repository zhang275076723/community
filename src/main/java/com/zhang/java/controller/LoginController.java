package com.zhang.java.controller;

import com.zhang.java.domain.User;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.UserActivationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

/**
 * @Date 2022/4/5 17:51
 * @Author zsy
 * @Description
 */
@Controller
public class LoginController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);

        //注册成功
        if (map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }

        //注册失败
        model.addAttribute("usernameMsg", map.get("usernameMsg"));
        model.addAttribute("passwordMsg", map.get("passwordMsg"));
        model.addAttribute("emailMsg", map.get("emailMsg"));
        return "/site/register";
    }

    // http://localhost:8080/community/activation/101/code
    @GetMapping("/activation/{userId}/{activationCode}")
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("activationCode") String activationCode) {

        int result = userService.activation(userId, activationCode);

        if (result == UserActivationStatus.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == UserActivationStatus.ACTIVATION_REPEAT) {
            model.addAttribute("msg", "重复操作，该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，请点击邮箱中的链接进行激活!");
            model.addAttribute("target", "/index");
        }

        return "/site/operate-result";
    }
}
