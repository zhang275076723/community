package com.zhang.java.service.impl;

import com.sun.org.apache.regexp.internal.RE;
import com.zhang.java.domain.LoginTicket;
import com.zhang.java.domain.User;
import com.zhang.java.mapper.LoginTicketMapper;
import com.zhang.java.mapper.UserMapper;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityUtil;
import com.zhang.java.util.MailClient;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Date 2022/4/3 20:28
 * @Author zsy
 * @Description
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    //服务器域名
    private String domain;

    @Value("${server.servlet.context-path}")
    //项目路径名
    private String contextPath;

    @Override
    public User findUserById(Integer id) {
        User user = getUserFromCache(id);
        if (user == null) {
            user = initUserFromCache(id);
        }
        return user;
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.selectUserByName(username);
    }

    /**
     * 登录成功，将登录凭证存储在redis中(不设置过期时间，通过登录凭证中的status判断当前凭证是否有效)
     *
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    @Override
    public Map<String, Object> login(String username, String password, Integer expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectUserByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证激活状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.encodeMD5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        // 将expiredSeconds转化为long，避免乘上1000时溢出，expiredSeconds单位为s，所以转换为ms要乘上1000
        loginTicket.setExpired(new Date(System.currentTimeMillis() + (long) expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        //redis保存用户登录凭证，redis会自动将loginTicket对象序列化为json格式的字符串
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(loginTicketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
//        loginTicketMapper.updateLoginTicketStatus(ticket, 1);
        //从redis中取用户登录凭证，并设置凭证状态为1，失效
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(loginTicketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(loginTicketKey, loginTicket);
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
    public Integer activation(Integer userId, String activationCode) {
        User user = userMapper.selectUserById(userId);
        if (user.getStatus() == 1) {
            return CommunityConstant.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateUserStatus(userId, 1);
            //对用户进行修改，需要清除用户缓存
            clearUserFromCache(userId);
            return CommunityConstant.ACTIVATION_SUCCESS;
        } else {
            return CommunityConstant.ACTIVATION_FAILURE;
        }
    }

    @Override
    public LoginTicket findLoginTicketByTicket(String ticket) {
//        return loginTicketMapper.selectLoginTicketByTicket(ticket);
        //从redis中获取用户登录凭证
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(loginTicketKey);
    }

    @Override
    public Integer updateHeader(Integer id, String headerUrl) {
        Integer rows = userMapper.updateUserHeader(id, headerUrl);
        //对用户进行修改，需要清除用户缓存
        clearUserFromCache(id);
        return rows;
    }

    @Override
    public Integer updatePassword(Integer id, String password) {
        User user = userMapper.selectUserById(id);
        password = CommunityUtil.encodeMD5(password + user.getSalt());
        Integer rows = userMapper.updateUserPassword(id, password);
        //对用户进行修改，需要清除用户缓存
        clearUserFromCache(id);
        return rows;
    }

    /**
     * 优先从redis中取用户
     *
     * @param userId
     * @return
     */
    private User getUserFromCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * redis中取不到用户时，从数据库中查询用户，并将用户放入redis(1h过期)
     *
     * @param userId
     * @return
     */
    private User initUserFromCache(int userId) {
        User user = userMapper.selectUserById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 用户数据变更时清除redis缓存数据，保证用户和redis数据的一致性
     *
     * @param userId
     */
    private void clearUserFromCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
