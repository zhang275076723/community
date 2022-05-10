package com.zhang.java.controller;

import com.zhang.java.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @Date 2022/5/10 19:26
 * @Author zsy
 * @Description
 */
@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    /**
     * 数据统计页面
     *
     * @return
     */
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    /**
     * 统计指定日期范围内的UV(独立访客)
     * 根据ip统计
     *
     * @param start
     * @param end
     * @param model
     * @return
     */
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                        //页面上是"yyyy-MM-dd"格式的日期字符串，需要指定日期字符串的格式，Spring才好把字符串转为Date
                        Model model) {
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        //因为要回显起始和结束日期，所以要传递start和end
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);

        //不能用redirect，因为redirect不能共享request域中数据
        return "forward:/data";
    }

    /**
     * 统计指定日期范围内的DAU(活跃用户)
     * 根据用户id统计
     *
     * @param start
     * @param end
     * @param model
     * @return
     */
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                         Model model) {
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        //因为要回显起始和结束日期，所以要传递start和end
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);

        //不能用redirect，因为redirect不能共享request域中数据
        return "forward:/data";
    }
}
