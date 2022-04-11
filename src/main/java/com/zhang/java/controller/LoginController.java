package com.zhang.java.controller;

import com.google.code.kaptcha.Producer;
import com.zhang.java.domain.User;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.UserActivationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @Date 2022/4/5 17:51
 * @Author zsy
 * @Description
 */
@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

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

    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        session.setAttribute("kaptcha", text);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            // 图片要用字节流
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

}
