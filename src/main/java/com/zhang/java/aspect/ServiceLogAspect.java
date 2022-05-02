package com.zhang.java.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Date 2022/4/22 17:12
 * @Author zsy
 * @Description service业务逻辑切面类
 */
@Aspect
@Component
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.zhang.java.service.impl.*.*(..))")
    public void pointcut() {
    }

    /**
     * 用户[1.2.3.4],在[xxx],访问了[com.zhang.java.community.impl.xxxService.xxx()].
     *
     * @param joinPoint
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        //在事件消费者中调用了messageService，所以获取不到attributes，直接返回
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        //如果使用localhots，则会显示ipv6地址：0:0:0:0:0:0:0:1；使用127.0.0.1，则会显示ipv4地址：127.0.0.1
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //joinPoint.getSignature().getDeclaringTypeName()：包名
        //joinPoint.getSignature().getName()：方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }

}
