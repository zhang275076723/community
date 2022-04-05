package com.zhang.java;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.mapper.UserMapper;
import com.zhang.java.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

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
        DiscussPostService discussPostService = (DiscussPostService) applicationContext.getBean("discussPostMapperService");
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
}
