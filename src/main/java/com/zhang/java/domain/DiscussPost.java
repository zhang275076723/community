package com.zhang.java.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


import java.util.Date;

/**
 * @Date 2022/4/3 19:31
 * @Author zsy
 * @Description 帖子
 * Document指定索引(indexName，必须小写)，类型(7.x版本没有类型type)，分片(shards)，副本(replicas)
 * 集群：多台es服务器合在一起，集群式部署提高性能
 * 节点：集群中的一台服务器
 * 分片(shards)：把一个索引（即一张表），拆分成多个分片去存，提高并发能力
 * 副本(replicas)：是对分片的备份，一个分片可以包含多个副本，提高可用性
 * 在访问es服务器时，Document会自动将实体数据和es服务器索引进行映射
 * Field指定实体属性和索引字段对应，type(属性类型)，analyzer(建立索引存储使用的分析器)，searchAnalyzer(搜索使用的分析器)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "discusspost"/*必须小写*/, /*type = "_doc",*/ shards = 6, replicas = 3)
public class DiscussPost {
    @Id
    private Integer id;

    /**
     * 创建帖子的用户id
     */
    @Field(type = FieldType.Integer)
    private Integer userId;

    /**
     * 帖子标题
     * 例子：互联网校招
     * analyzer：存储时拆分为尽可能多个词条，增加搜索范围，例如：互联网、校招、互联、联网、网校
     * searchAnalyzer：查询时拆分为尽可能少的词条，能够满足要求的词条，例如：互联网、校招
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 帖子内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 帖子类型：
     * 0-普通
     * 1-置顶
     */
    @Field(type = FieldType.Integer)
    private Integer type;

    /**
     * 帖子状态：
     * 0-正常
     * 1-精华
     * 2-拉黑(删除)
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 帖子创建时间
     */
    @Field(type = FieldType.Date)
    private Date createTime;

    /**
     * 帖子评论数量，只包括帖子的评论数量，不包括帖子评论中的评论数量
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /**
     * 帖子分数
     */
    @Field(type = FieldType.Double)
    private Double score;
}
