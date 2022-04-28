package com.zhang.java.controller;

import com.google.code.kaptcha.Producer;
import com.zhang.java.annotation.LoginRequired;
import com.zhang.java.domain.User;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    //项目路径名
    private String contextPath;

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 用户登录
     * 从cookie中获取验证码的临时凭证，通过验证码的临时凭证在redis中获取验证码
     *
     * @param model
     * @param response
     * @param user 登录用户
     * @param verifyCode 登录时输入的验证码
     * @param kaptchaOwner 验证码的临时凭证
     * @param rememberMe 是否记住我
     * @return
     */
    @PostMapping("/login")
    public String login(Model model,
                        HttpServletResponse response,
                        User user,
                        @RequestParam("verifyCode") String verifyCode,
                        @CookieValue("kaptchaOwner") String kaptchaOwner,
                        boolean rememberMe) {
        //因为获取的是checkbox中的checked属性值，而不是checkbox中的value值，所以不能使用@RequestParam

        String kaptcha = null;

        //从redis中获取验证码
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        // 检查验证码
        if (StringUtils.isBlank(kaptcha) ||
                StringUtils.isBlank(verifyCode) ||
                !kaptcha.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("verifyCodeMsg", "验证码不正确!");
            // 因为rememberMe是请求参数，在页面回显时，可以不将其放入model中，
            // 而直接在thymeleaf页面中使用${param.rememberMe}，同样也能获取该值
            // param表示本次请求
            model.addAttribute("rememberMe", rememberMe);
            //不能重定向到get请求的login，因为model携带的数据只在本次request中有效
            return "/site/login";
        }

        // 检查用户名，密码
        // 根据是否记住我设置用户登录凭证的超时时间
        int expiredSeconds = rememberMe ?
                CommunityConstant.REMEMBER_EXPIRED_SECONDS :
                CommunityConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(user.getUsername(), user.getPassword(), expiredSeconds);
        if (map.containsKey("ticket")) {
            //设置cookie，其中的loginTicket用于保存用户登录状态
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            //重定向到get请求的index
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            // 因为rememberMe是请求参数，在页面回显时，可以不将其放入model中，
            // 而直接在thymeleaf页面中使用${param.rememberMe}，同样也能获取该值
            // param表示本次请求
            model.addAttribute("rememberMe", rememberMe);
            //不能重定向到get请求的login，因为model携带的数据只在本次request中有效
            return "/site/login";
        }
    }

    //使用自定义注解实现请求拦截
    @LoginRequired
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);

        //重定向到get请求的index
        return "redirect:/index";
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

        //不能使用重定向，因为model携带的数据只在本次request中有效
        return "/site/register";
    }

    /**
     * 激活用户
     * http://localhost:8080/community/activation/101/code
     *
     * @param model
     * @param userId
     * @param activationCode
     * @return
     */
    @GetMapping("/activation/{userId}/{activationCode}")
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("activationCode") String activationCode) {

        int result = userService.activation(userId, activationCode);

        if (result == CommunityConstant.ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == CommunityConstant.ACTIVATION_REPEAT) {
            model.addAttribute("msg", "重复操作，该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，请点击邮箱中的链接进行激活!");
            model.addAttribute("target", "/index");
        }

        return "/site/operate-result";
    }

    /**
     * 获取验证码
     * 使用redis存储验证码，将验证码的临时凭证存放在cookie中，用于登录验证时从cookie中获取(60s过期)
     *
     * @param response
     */
    @GetMapping(path = "/kaptcha")
    public void getKaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        // 生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        //随机生成验证码的临时凭证，存放在cookie中
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将验证码存储Redis，并设置超时时间
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器，设置响应类型为图片类型
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
