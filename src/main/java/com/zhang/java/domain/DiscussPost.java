package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/3 19:31
 * @Author zsy
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscussPost {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    //帖子类型，0-普通，1-置顶
    private Integer type;
    //帖子状态，0-正常，1-精华，2-拉黑
    private Integer status;
    private Date createTime;
    private Integer commentCount;
    private Double score;
}
