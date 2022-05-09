package com.zhang.java.interceptor;

import com.zhang.java.domain.User;
import com.zhang.java.service.MessageService;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Date 2022/5/2 22:36
 * @Author zsy
 * @Description 每次请求都要统计朋友私信+系统通知的总数量，显示为消息的数量
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        //当前用户已登录，且需要跳转到目标页面时，才进行查询用户未读的消息总数
        //如果没有添加modelAndView != null判断，有可能ajax请求不返回页面，导致modelAndView为null，抛出异常
        if (user != null && modelAndView != null) {
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("totalUnreadCount",
                    letterUnreadCount + noticeUnreadCount);
        }
    }

}
