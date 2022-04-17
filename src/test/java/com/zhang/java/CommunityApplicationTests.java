package com.zhang.java;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.mapper.LoginTicketMapper;
import com.zhang.java.mapper.MessageMapper;
import com.zhang.java.mapper.UserMapper;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.Impl.TestService;
import com.zhang.java.util.MailClient;
import com.zhang.java.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
//使用指定类作为配置类
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);
    }

    @Test
    public void testUserMapper() {
        UserMapper userMapper = (UserMapper) applicationContext.getBean("userMapper");
        System.out.println(userMapper);
        System.out.println(userMapper.selectUserById(101));
    }

    @Test
    public void testPageHelper() {
        PageHelper.startPage(1, 10);
        DiscussPostService discussPostService = (DiscussPostService) applicationContext.getBean("discussPostServiceImpl");
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0);
        PageInfo<DiscussPost> pageInfo = new PageInfo<>(discussPosts, 5);
        System.out.println(pageInfo);
    }

    @Test
    public void testLogger() {
        Logger logger = LoggerFactory.getLogger(CommunityApplicationTests.class);
        System.out.println(logger);
        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");
    }

    @Test
    public void testSendEmail() {
        MailClient mailClient = (MailClient) applicationContext.getBean("mailClient");
        mailClient.sendMail("275076723@qq.com", "测试", "哈哈");
    }

    @Test
    public void testSendHtmlEmail() {
        TemplateEngine templateEngine = (TemplateEngine) applicationContext.getBean("templateEngine");
        MailClient mailClient = (MailClient) applicationContext.getBean("mailClient");

        Context context = new Context();
        //设置html中username的值
        context.setVariable("username", "Kat");
        String content = templateEngine.process("/mail/html_demo", context);
        System.out.println(content);

        mailClient.sendMail("275076723@qq.com", "html测试", content);
    }

    @Test
    public void testLoginTicket() {
        LoginTicketMapper loginTicketMapper = (LoginTicketMapper) applicationContext.getBean("loginTicketMapper");
//        loginTicketMapper.insertLoginTicket(new LoginTicket(null, 1001, "abcde", 0, new Date(System.currentTimeMillis() + 1000 * 60 * 10)));
        loginTicketMapper.updateLoginTicketStatus("abcde", 1);
        System.out.println(loginTicketMapper.selectLoginTicketByTicket("abcde"));
    }

    @Test
    public void testSensitiveWords() {
        SensitiveFilter sensitiveFilter = (SensitiveFilter) applicationContext.getBean("sensitiveFilter");
        String text = "碰❤瓷嫖❤娼，卖艺❤卖❤淫❤";
        System.out.println(sensitiveFilter.filterSensitiveWords(text));
    }

    @Test
    public void testTransaction() {
        TestService testService = applicationContext.getBean("testService", TestService.class);
        Object save = testService.save();
        System.out.println(save);
    }

    @Test
    public void testMessageMapper(){
        MessageMapper messageMapper = applicationContext.getBean("messageMapper", MessageMapper.class);
        System.out.println(messageMapper.selectConversations(111));
        System.out.println(messageMapper.selectConversationCount(111));
        System.out.println(messageMapper.selectLetters("111_112"));
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(111, "111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(111, null));
    }
}
