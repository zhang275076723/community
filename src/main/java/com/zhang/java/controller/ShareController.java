package com.zhang.java.controller;

import com.zhang.java.domain.Event;
import com.zhang.java.event.EventProducer;
import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Date 2022/5/12 15:54
 * @Author zsy
 * @Description
 */
@Controller
public class ShareController {
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    /**
     * 域名
     */
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 项目名
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * wk图片存放路径
     */
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * 生成长图
     *
     * @param htmlUrl
     * @return
     */
    @GetMapping("/share")
    @ResponseBody
    public String share(@RequestParam("htmlUrl") String htmlUrl) {
        //文件名
        String fileName = CommunityUtil.generateUUID() + ".png";

        //生成长图分享事件，需要在命令行手动启动kafka、zookeeper
        Event event = new Event();
        event.setTopic(CommunityConstant.TOPIC_SHARE);
        event.setData("htmlUrl", htmlUrl);
        event.setData("fileName", fileName);
        //触发生成长图分享事件
        eventProducer.fireEvent(event);

        Map<String, Object> map = new HashMap<>();
        //生成长图的访问路径
        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);

        return CommunityUtil.getJSONString(0, "生成长图成功", map);
    }

    /**
     * 显示生成的长图
     * http://localhost:8080/community/share/image/73ebfe2dd07f4eee96bf583523d5f33d.png
     *
     * @param fileName
     * @param response
     */
    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空!");
        }

        //设置响应为图片类型
        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName);

        FileInputStream fis = null;
        OutputStream os = null;
        try {
            fis = new FileInputStream(file);
            os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = fis.read(buffer)) != -1) {
                os.write(buffer, 0, size);
            }
        } catch (IOException e) {
            logger.error("获取长图失败: " + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
