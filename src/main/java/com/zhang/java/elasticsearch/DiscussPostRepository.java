package com.zhang.java.elasticsearch;

import com.zhang.java.domain.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Date 2022/5/3 19:18
 * @Author zsy
 * @Description es查询
 * ElasticsearchRepository：当前接口已经定义好了对es服务器的增删改查各种方法。Spring会给它自动做一个实现，直接调用即可。
 * DiscussPost：接口要处理的实体类
 * Integer：实体类中主键的类型
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
