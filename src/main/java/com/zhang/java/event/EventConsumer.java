package com.zhang.java.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.Event;
import com.zhang.java.domain.Message;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.service.DiscussPostService;
import com.zhang.java.service.ElasticsearchService;
import com.zhang.java.service.MessageService;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * @Date 2022/5/1 17:37
 * @Author zsy
 * @Description 事件消费者
 */
@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    /**
     * wk生成长图命令
     */
    @Value("${wk.image.command}")
    private String wkImageCommand;

    /**
     * 本地图片存放路径
     */
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * Spring能够启动定时任务的线程池，分布式环境下有问题
     */
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

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
     * 消费生成长图分享事件，将生成的图片存放到本地服务器和七牛云服务器
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
            //生成的图片存放到本地服务器
            Runtime.getRuntime().exec(command);
            logger.info("生成长图成功: " + command);
        } catch (IOException e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

        //启用定时器，监视该长图是否生成，一旦生成了，则上传至七牛云服务器
        UploadImageTask uploadImageTask = new UploadImageTask(fileName);
        //scheduledFuture-启动任务的返回值，可以终止任务
        //period—任务连续执行之间的间隔(毫秒)
        ScheduledFuture scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(
                uploadImageTask, 500);
        uploadImageTask.setFuture(scheduledFuture);
    }

    /**
     * 监视长图是否生成的定时任务，
     */
    private class UploadImageTask implements Runnable {
        //要上传的文件名
        private String fileName;
        //上传任务开始时间
        private long startTime;
        //上传次数
        private int uploadTimes;
        //启动定时任务的返回值，可以终止任务
        private Future future;

        public UploadImageTask(String fileName) {
            this.fileName = fileName;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        /**
         * 上传次数超过3次，上传时间超过30s都设置为上传失败
         */
        @Override
        public void run() {
            // 生成失败
            if (System.currentTimeMillis() - startTime > 1000 * 30) {
                logger.error("执行时间过长，终止上传图片到七牛云的任务：" + fileName);
                //上传失败，终止定时任务
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3) {
                logger.error("上传次数过多，终止上传图片到七牛云的任务：" + fileName);
                //上传失败，终止定时任务
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName;
            File file = new File(path);

            //当前图片存在才上传到七牛云服务器
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传图片到七牛云[%s].", ++uploadTimes, fileName));
                // 设置响应信息，七牛云返回的json数据
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0, null, null));
                // 生成上传凭证 1小时过期
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Region.huadong()));

                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/png", false);
                    // 处理响应结果，是returnBody返回的json
                    JSONObject json = JSONObject.parseObject(response.bodyString());

                    if (json == null || json.get("code") == null || !"0".equals(json.get("code").toString())) {
                        logger.info(String.format("第%d次上传图片到七牛云失败[%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第%d次上传图片到七牛云成功[%s].", uploadTimes, fileName));
                        //上传成功，终止定时任务
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传图片到七牛云失败[%s].", uploadTimes, fileName));
                }
            } else {
                logger.info("等待图片生成[" + fileName + "].");
            }
        }
    }
}
