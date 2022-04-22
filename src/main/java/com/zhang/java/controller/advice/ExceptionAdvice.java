package com.zhang.java.controller.advice;

import com.zhang.java.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Date 2022/4/21 19:24
 * @Author zsy
 * @Description 异常处理类，普通请求错误跳转到500页面，ajax请求错误提示错误消息，没有匹配的请求跳转到404页面
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class)
    public void handlerException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        logger.error("服务器发生异常: " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        //x-requested-with请求头：区分请求是ajax请求还是普通请求
        String xRequestedWith = request.getHeader("x-requested-with");
        //ajax请求
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            //设置响应的内容类型
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer;
            try {
                writer = response.getWriter();
                writer.write(CommunityUtil.getJSONString(1, "服务器异常！", null));
            } catch (IOException ex) {
                logger.error("ajax请求异常：" + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            //普通请求
            try {
                //默认错误页面
                response.sendRedirect(request.getContextPath() + "/error");
            } catch (IOException ex) {
                logger.error("普通请求异常：" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

}
