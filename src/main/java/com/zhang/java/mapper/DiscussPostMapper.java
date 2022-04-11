package com.zhang.java.mapper;

import com.zhang.java.domain.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Date 2022/4/3 19:34
 * @Author zsy
 * @Description
 */
@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(@Param("userId") Integer userId);

    Integer selectDiscussPostRows(@Param("userId") Integer userId);

}
