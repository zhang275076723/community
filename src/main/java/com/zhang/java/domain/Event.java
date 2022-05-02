package com.zhang.java.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Date 2022/5/1 16:42
 * @Author zsy
 * @Description 消息事件
 */
@Data
public class Event {
    /**
     * 事件主题
     */
    private String topic;

    /**
     * 事件触发者id
     */
    private int userId;

    /**
     * 事件实体类型
     */
    private int entityType;

    /**
     * 事件实体id
     */
    private int entityId;

    /**
     * 事件实体用户id，当前实体是由哪个用户所创建的
     */
    private int entityUserId;

    /**
     * 事件其他数据放到map中，便于扩展
     */
    private Map<String, Object> data = new HashMap<>();

    public void setData(String key, Object value) {
        data.put(key, value);
    }
}
