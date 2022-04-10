package com.zhang.java.service.Impl;

import com.zhang.java.domain.User;
import com.zhang.java.mapper.UserMapper;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.MailClient;
import com.zhang.java.util.UserActivationStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Date 2022/4/3 20:28
 * @Author zsy
 * @Description
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    //服务器域名
    private String domain;

    @Value("${server.servlet.context-path}")
    //项目名
    private String contextPath;

    @Override
    public User findUserById(int id) {
        return userMapper.selectUserById(id);
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("用户参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectUserByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectUserByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //加密之前在密码后面添加一些内容再加密，保证安全性
        user.setPassword(CommunityUtil.encodeMD5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        //激活码，32位uuid
        user.setActivationCode(CommunityUtil.generateUUID());
        //0-1000的随机值
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1001)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发生激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/activationCode
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("registerUrl", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    @Override
    public int activation(int userId, String activationCode) {
        User user = userMapper.selectUserById(userId);
        if (user.getStatus() == 1) {
            return UserActivationStatus.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateUserStatus(userId, 1);
            return UserActivationStatus.ACTIVATION_SUCCESS;
        } else {
            return UserActivationStatus.ACTIVATION_FAILURE;
        }
    }

}
