package com.zhang.java.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Date 2022/4/13 17:24
 * @Author zsy
 * @Description 使用自定义注解拦截请求，使用spring security替代
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoginRequired {

}
