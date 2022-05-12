package com.zhang.java.event;

import com.alibaba.fastjson.JSONObject;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.Event;
import com.zhang.java.domain.Message;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.ElasticsearchService;
import com.zhang.java.service.MessageService;
import com.zhang.java.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Date 2022/5/1 17:37
 * @Author zsy
 * @Description 事件消费者
 */
@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 消费评论、点赞、关注事件，发送系统消息
     *
     * @param record
     */
    @KafkaListener(topics = {
            CommunityConstant.TOPIC_COMMENT,
            CommunityConstant.TOPIC_LIKE,
            CommunityConstant.TOPIC_FOLLOW
    })
    public void handleMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        //获取消息事件内容
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        //系统消息
        Message message = new Message();
        message.setFromId(CommunityConstant.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setStatus(0);
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        //事件触发者id，事件实体类型，事件实体id，事件其他数据，用于系统消息的显示和消息点击跳转
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
            content.put(entry.getKey(), entry.getValue());
        }
        //系统消息内容为json格式
        message.setContent(JSONObject.toJSONString(content));

        //发送系统消息
        messageService.addMessage(message);
    }

    /**
     * 消费发帖、置顶帖子、帖子加精事件，将帖子存放到es中
     *
     * @param record
     */
    @KafkaListener(topics = CommunityConstant.TOPIC_PUBLISH)
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        //获取消息事件内容
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }

    /**
     * 消费删帖事件，将帖子从es中删除
     *
     * @param record
     */
    @KafkaListener(topics = CommunityConstant.TOPIC_DELETE)
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        //获取消息事件内容
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    /**
     * 消费生成长图分享事件，将生成的图片存放到指定位置
     *
     * @param record
     */
    @KafkaListener(topics = CommunityConstant.TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        //获取消息事件内容
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");

        String command = wkImageCommand + " --quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName;
        try {
            //命令由操作系统执行，java程序不会等待命令执行完成之后才继续执行，两者是并发的
            Runtime.getRuntime().exec(command);
            logger.info("生成长图成功: " + command);
        } catch (IOException e) {
            logger.error("生成长图失败: " + e.getMessage());
        }
    }
}
