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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
     * 消费发帖事件，将帖子存放到es中
     *
     * @param record
     */
    @KafkaListener(topics = CommunityConstant.TOPIC_PUBLISH)
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);
    }
}
