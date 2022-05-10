package com.zhang.java.interceptor;

import com.zhang.java.domain.User;
import com.zhang.java.service.DataService;
import com.zhang.java.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Date 2022/5/10 19:09
 * @Author zsy
 * @Description 记录每次访问的独立访客(UV)和活跃用户(DAU)到redis中
 */
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //记录每次访问的独立访客(UV)
        //如果使用localhost，则会显示ipv6地址：0:0:0:0:0:0:0:1；使用127.0.0.1，则会显示ipv4地址：127.0.0.1
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        //记录每次访问的活跃用户(DAU)
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
