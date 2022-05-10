package com.zhang.java.service;

import java.util.Date;

/**
 * @Date 2022/5/10 16:59
 * @Author zsy
 * @Description
 */
public interface DataService {
    /**
     * 将ip记入到当天的UV
     *
     * @param ip
     */
    void recordUV(String ip);

    /**
     * 统计指定日期范围内的UV
     * 通过将范围HyperLogLog合并，进行统计
     *
     * @param start
     * @param end
     * @return
     */
    long calculateUV(Date start, Date end);

    /**
     * 将userId记入到当天的DAU
     *
     * @param userId
     */
    void recordDAU(int userId);

    /**
     * 统计指定日期范围内的DAU
     * 通过将范围Bitmap进行或运算，进行统计
     *
     * @param start
     * @param end
     * @return
     */
    long calculateDAU(Date start, Date end);
}
