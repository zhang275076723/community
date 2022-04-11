package com.zhang.java.controller;

import com.zhang.java.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Date 2022/4/11 14:44
 * @Author zsy
 * @Description
 */
@Controller
public class TestController {

    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie的生效范围
        cookie.setPath(request.getContextPath()+"/cookie");
        System.out.println(request.getContextPath());
        //设置cookie的生存时间，单位s，存放在硬盘中
        cookie.setMaxAge(60 * 10);
        response.addCookie(cookie);
        return "set cookie";
    }

    @GetMapping(path = "/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }

    @GetMapping(path = "/session/set")
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id", 1001);
        session.setAttribute("name", "Kat");
        return "set session";
    }

    @GetMapping(path = "/session/get")
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

}
