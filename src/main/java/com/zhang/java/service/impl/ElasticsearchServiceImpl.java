package com.zhang.java.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhang.java.domain.DiscussPost;
import com.zhang.java.elasticsearch.DiscussPostRepository;
import com.zhang.java.mapper.DiscussPostMapper;
import com.zhang.java.service.ElasticsearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Date 2022/5/5 16:54
 * @Author zsy
 * @Description
 */
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    @Override
    public void deleteDiscussPost(int id) {
        discussPostRepository.delete(discussPostMapper.selectDiscussPostById(id));
    }

    @Override
    public long searchDiscussPostCount(String keyword) throws IOException {
        //discusspost是索引名，就是表名
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                //从title和content中查询keyword
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        return searchResponse.getHits().getTotalHits().value;
    }

    @Override
    public List<DiscussPost> searchDiscussPost(String keyword, int offset, int limit) throws IOException {
        //discusspost是索引名，就是表名
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        //global.css对em标签进行了定义，会显示红色
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                //从title和content中查询keyword
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //指定从哪条开始查询
                .from(offset)
                //需要查出的总记录条数
                .size(limit)
                //高亮
                .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }

            list.add(discussPost);
        }

        return list;
    }
}
