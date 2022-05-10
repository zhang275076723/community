package com.zhang.java.config;

import com.zhang.java.util.CommunityConstant;
import com.zhang.java.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Date 2022/5/7 20:44
 * @Author zsy
 * @Description spring security配置类
 */
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        //对静态资源不拦截
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                //当前是什么用户，可以访问哪些路径
                .antMatchers(
                        "/user/setting",
                        "/user/uploadHeaderImage",
                        "/user/updatePassword",
                        "/logout",
                        "/comment/add/**",
                        "/discussPost/add",
                        "/follow",
                        "/unfollow",
                        "/like",
                        "/letter/**",
                        "/notice/**",
                        "/letter/send"
                )
                .hasAnyAuthority(
                        CommunityConstant.AUTHORITY_USER,
                        CommunityConstant.AUTHORITY_ADMIN,
                        CommunityConstant.AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discussPost/top",
                        "/discussPost/wonderful"
                )
                .hasAnyAuthority(CommunityConstant.AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discussPost/delete",
                        "/data/**"
                )
                .hasAnyAuthority(CommunityConstant.AUTHORITY_ADMIN)
                //其他任意请求都允许访问
                .anyRequest().permitAll()
                //不启用防止csrf攻击，默认启用防止csrf攻击
                //对于表单提交，会自动生成隐含的token，name=_csrf，value=21f5944d-4c4a-4b6f-b41c-60fc0b393c81
                //对于ajax请求，必须手动添加token，示例：index.html中的发布帖子
                .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling()
                // 没有登录
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
                        //x-requested-with请求头：区分请求是ajax请求还是普通请求
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //ajax请求
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            //设置响应的内容类型
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "您还没有登录哦!", null));
                        } else { //普通请求
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                // 权限不足
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
                        //x-requested-with请求头：区分请求是ajax请求还是普通请求
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //ajax请求
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "您没有访问此功能的权限!", null));
                        } else { //普通请求
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        // 退出相关配置
        http.logout()
                //退出登录拦截的请求路径，Security底层默认会拦截/logout请求，进行退出处理.
                //覆盖它默认的逻辑,才能执行自己的退出代码.
                .logoutUrl("/securitylogout");
    }

}
