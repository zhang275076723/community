package com.zhang.java.event;

import com.alibaba.fastjson.JSONObject;
import com.zhang.java.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @Date 2022/5/1 17:33
 * @Author zsy
 * @Description 事件生产者
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件，将事件发布到指定的主题
     * 发送json格式的事件
     *
     * @param event
     */
    public void fireEvent(Event event) {
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
