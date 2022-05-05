package com.zhang.java;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.elasticsearch.DiscussPostRepository;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.mapper.LoginTicketMapper;
import com.zhang.java.mapper.MessageMapper;
import com.zhang.java.mapper.UserMapper;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.impl.TestService;
import com.zhang.java.util.MailClient;
import com.zhang.java.util.SensitiveFilter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@SpringBootTest
//使用指定类作为配置类
@ContextConfiguration(classes = CommunityApplication.class)
@EnableElasticsearchRepositories(basePackages = "com.zhang.java.elasticsearch")
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
    public void testMessageMapper() {
        MessageMapper messageMapper = applicationContext.getBean("messageMapper", MessageMapper.class);
        System.out.println(messageMapper.selectConversations(111));
        System.out.println(messageMapper.selectConversationCount(111));
        System.out.println(messageMapper.selectLetters("111_112"));
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(111, "111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(111, null));
    }

    @Test
    public void testRedis() {
        RedisTemplate redisTemplate = applicationContext.getBean("redisTemplate", RedisTemplate.class);
        System.out.println(redisTemplate);
        System.out.println("------------------------------------------------------------");

        //string
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println("------------------------------------------------------------");

        //list
        redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 1001);
        redisTemplate.opsForList().leftPush(redisKey, 1002);
        redisTemplate.opsForList().rightPush(redisKey, 1003);
        redisTemplate.opsForList().set(redisKey, 0, 1009);
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 1));
        System.out.println("------------------------------------------------------------");

        //hash
        redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "username", "Kat");
        redisTemplate.opsForHash().put(redisKey, "password", 123);
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
        System.out.println(redisTemplate.opsForHash().values(redisKey));
        System.out.println("------------------------------------------------------------");

        //set
        redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "诸葛亮");
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
        System.out.println("------------------------------------------------------------");

        //zset
        redisKey = "test:students";
        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 50);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 70);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 60);
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey, "悟空"));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "沙僧"));
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println("------------------------------------------------------------");

        //key
        redisKey = "test:count";
        System.out.println(redisTemplate.type(redisKey));
        System.out.println(redisTemplate.keys("*"));
        System.out.println("------------------------------------------------------------");

        //transaction
        Object result = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                //开启事务
                operations.multi();
                operations.opsForSet().add(redisKey, "sid", "tom", "jack");
                //事务中进行查询，无效
                System.out.println(operations.opsForSet().members(redisKey));
                //提交事务
                return operations.exec();
            }
        });
        System.out.println(result);
        System.out.println("------------------------------------------------------------");
    }

    @Test
    public void testBlockingQueue() {
        //阻塞队列，大小为3
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);

        //生产者
        new Thread(new Runnable() {
            @Override
            public void run() {
                //生产10个数据
                for (int i = 1; i <= 10; i++) {
                    try {
                        int data = new Random().nextInt(10);
                        System.out.println(Thread.currentThread().getName() + "第" + i + "生产数据：" + data);
                        queue.put(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "producer").start();

        //消费者
        new Thread(new Runnable() {
            @Override
            public void run() {
                //消费10个数据
                for (int i = 1; i <= 10; i++) {
                    try {
                        Integer data = queue.take();
                        System.out.println(Thread.currentThread().getName() + "第" + i + "消费数据：" + data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "consumer").start();
    }

    /**
     * 需要在命令行手动启动kafka、zookeeper
     * 1.开启zookeeper：D:\kafka_2.12-3.1.0>bin\windows\zookeeper-server-start.bat config\zookeeper.properties
     * 2.开启kafka：D:\kafka_2.12-3.1.0>bin\windows\kafka-server-start.bat config\server.properties
     * 3.创建topic：D:\kafka_2.12-3.1.0\bin\windows>kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
     * 4.生产者：D:\kafka_2.12-3.1.0\bin\windows>kafka-console-producer.bat --broker-list localhost:9092 --topic test
     * 5.消费者：D:\kafka_2.12-3.1.0\bin\windows>kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --from-beginning
     * 6.关闭kafka：D:\kafka_2.12-3.1.0\bin\windows>kafka-server-stop.bat
     * 7.开启zookeeper：D:\kafka_2.12-3.1.0\bin\windows>zookeeper-server-stop.bat
     */
    @Test
    public void kafkaTest() {
        KafkaProducer kafkaProducer = applicationContext.getBean("kafkaProducer", KafkaProducer.class);

        kafkaProducer.sendMessage("test", "hi");
        kafkaProducer.sendMessage("test", "hello");

        try {
            Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 需要启动es服务器，D:\elasticsearch-7.15.2\bin\elasticsearch.bat
     */
    @Test
    public void elasticsearchTest() throws IOException {
        DiscussPostMapper discussPostMapper = applicationContext.getBean(
                "discussPostMapper", DiscussPostMapper.class);
        DiscussPostRepository discussPostRepository = applicationContext.getBean(
                "discussPostRepository", DiscussPostRepository.class);
        ElasticsearchRestTemplate elasticsearchRestTemplate = applicationContext.getBean(
                "elasticsearchTemplate", ElasticsearchRestTemplate.class);
        RestHighLevelClient restHighLevelClient = applicationContext.getBean(
                "restHighLevelClient", RestHighLevelClient.class);

        //判断某id的文档(相当于数据库中的行)是否存在
//        System.out.println(discussPostRepository.existsById(109));

        //一次保存一条数据
        //保存帖子id为241的DiscussPost到discusspost索引(es的索引相当于数据库的表)
//        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
//        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
//        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));

        //一次保存多条数据
        //保存用户id为101的全部DiscussPost到discusspost索引(es的索引相当于数据库的表)
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134));

        //覆盖原内容，来修改一条数据
//        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
//        discussPost.setContent("我是新人,使劲灌水.");
//        discussPostRepository.save(discussPost);

        //修改一条数据
        //修改es内容：设置title为null，则保持不变，不设置，则修改为空
        //覆盖es内容：设置title为null，则修改为空
//        UpdateRequest updateRequest = new UpdateRequest("discusspost", "109");
//        updateRequest.timeout("1s");
//        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(230);
//        discussPost.setContent("我是新人,使劲灌水.");
//        //es中的title保持不变
//        discussPost.setTitle(null);
//        updateRequest.doc(JSON.toJSONString(discussPost), XContentType.JSON);
//        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//        System.out.println(updateResponse.status());

        //删除一条数据
//        discussPostRepository.deleteById(109);
        //删除所有数据
//        discussPostRepository.deleteAll();

        //不带高亮查询，SearchSourceBuilder
//        SearchRequest searchRequest = new SearchRequest("discusspost");
//        //构建查询条件，在discusspost索引的title和content字段中都查询"互联网寒冬"
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
//                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                // matchQuery是模糊查询，会对key进行分词
//                // searchSourceBuilder.query(QueryBuilders.matchQuery(key, value));
//                // termQuery是精准查询
//                // searchSourceBuilder.query(QueryBuilders.termQuery(key, value));
//                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                //一个可选项，用于控制允许搜索的时间
//                // searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//                // 指定从哪条开始查询
//                .from(0)
//                // 需要查出的总记录条数
//                .size(10);
//        searchRequest.source(searchSourceBuilder);
//        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        System.out.println(JSONObject.toJSON(searchResponse));
//        for (SearchHit hit : searchResponse.getHits().getHits()) {
//            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
//            System.out.println(discussPost);
//        }

        //高亮查询，highlightBuilder
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //构建查询条件，在discusspost索引的title和content字段中都查询"互联网寒冬"
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 指定从哪条开始查询
                .from(0)
                // 需要查出的总记录条数
                .size(10)
                //高亮
                .highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            System.out.println(discussPost);
        }
    }
}

@Component
class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

@Component
class KafkaConsumer {
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}