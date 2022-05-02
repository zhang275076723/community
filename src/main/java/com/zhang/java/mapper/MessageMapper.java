package com.zhang.java.mapper;

import com.zhang.java.domain.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Date 2022/4/17 16:47
 * @Author zsy
 * @Description
 */
@Mapper
public interface MessageMapper {

    /**
     * 查询用户的私信列表，每个私信列表只返回最新一条的私信
     * 去除系统管理员的私信，去除已经删除的私信
     *
     * @param userId
     * @return
     */
    List<Message> selectConversations(@Param("userId") Integer userId);

    /**
     * 查询用户的私信列表数量
     *
     * @param userId
     * @return
     */
    Integer selectConversationCount(@Param("userId") Integer userId);

    /**
     * 查询某个私信列表中的私信
     *
     * @param conversationId
     * @return
     */
    List<Message> selectLetters(@Param("conversationId") String conversationId);

    /**
     * 查询某个私信列表中的私信数量
     *
     * @param conversationId
     * @return
     */
    Integer selectLetterCount(@Param("conversationId") String conversationId);

    /**
     * 1、查询用户未读私信的数量，conversationId为null
     * 2、查询用户某个私信列表未读私信的数量，conversationId不为null
     *
     * @param userId
     * @param conversationId
     * @return
     */
    Integer selectLetterUnreadCount(@Param("userId") Integer userId,
                                    @Param("conversationId") String conversationId);

    /**
     * 添加私信
     *
     * @param message
     * @return
     */
    Integer insertMessage(Message message);

    /**
     * 修改私信列表中所有私信的状态
     *
     * @param ids
     * @param status
     * @return
     */
    Integer updateMessageStatus(@Param("ids") List<Integer> ids,
                                @Param("status") Integer status);

    /**
     * 查询某个主题最新的系统通知，因为系统通知页面只显示最新的那条通知
     *
     * @param userId
     * @param topic
     * @return
     */
    Message selectLastestNotice(@Param("userId") int userId,
                                @Param("topic") String topic);

    /**
     * 查询某个主题所包含的通知数量
     *
     * @param userId
     * @param topic
     * @return
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * 1、查询某个主题未读的通知的数量，topic不为null
     * 2、查询所有主题未读的通知的数量，topic为null
     *
     * @param userId
     * @param topic
     * @return
     */
    int selectNoticeUnreadCount(int userId, String topic);

    /**
     * 查询某个主题所包含的所有系统通知
     *
     * @param userId
     * @param topic
     * @return
     */
    List<Message> selectNotices(int userId, String topic);
}
