package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Date 2022/4/16 16:25
 * @Author zsy
 * @Description 评论，包括帖子评论和帖子评论的评论
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    private Integer id;
    private Integer userId;
    //评论实体类型，1-帖子的评论，2-帖子评论的评论
    private Integer entityType;
    //评论实体id，entityType为1，帖子的id，表明是帖子的评论；entityType为2，评论的id，表明是帖子评论的评论
    private Integer entityId;
    //评论回复用户id，帖子评论的评论分为2种情况，一种是对帖子评论进行评论，不需要回复用户id；
    //另一种是对帖子评论进行回复，需要回复用户的id
    private Integer targetId;
    //评论内容
    private String content;
    //评论状态，0-正常，1-无效
    private Integer status;
    private Date createTime;
}
