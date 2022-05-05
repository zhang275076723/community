package com.zhang.java.controller;

import com.zhang.java.domain.DiscussPost;
import com.zhang.java.domain.Page;
import com.zhang.java.service.ElasticsearchService;
import com.zhang.java.service.LikeService;
import com.zhang.java.service.UserService;
import com.zhang.java.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Date 2022/5/5 17:47
 * @Author zsy
 * @Description
 */
@Controller
public class SearchController {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 根据关键字查询帖子，高亮显示匹配的帖子标题和内容
     * /search?keyword=xxx&pageNum=xxx
     *
     * @param keyword
     * @param pageNum
     * @param model
     * @return
     */
    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword,
                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                         Model model) throws IOException {
        //关键字查询帖子分页，使用自定义分页，不能使用PageHelper
        Page page = new Page();
        page.setPageSize(5);
        page.setTotalRows((int) elasticsearchService.searchDiscussPostCount(keyword));
        page.setTotalPages();
        page.setPageNum(pageNum);
        page.setUrlPath("/search?keyword=" + keyword);
        model.addAttribute("page", page);

        //帖子、作者、点赞数量相对应
        List<Map<String, Object>> discussPostsList = new ArrayList<>();
        List<DiscussPost> searchResults =
                elasticsearchService.searchDiscussPost(keyword, page.getOffset(), page.getPageSize());

        for (DiscussPost discussPost : searchResults) {
            Map<String, Object> map = new HashMap<>();
            map.put("discussPost", discussPost);
            map.put("user", userService.findUserById(discussPost.getUserId()));
            map.put("likeCount", likeService.findEntityLikeCount(
                    CommunityConstant.ENTITY_TYPE_DISCUSSPOST, discussPost.getId()));

            discussPostsList.add(map);
        }

        model.addAttribute("discussPostsList", discussPostsList);
        model.addAttribute("keyword", keyword);

        return "site/search";
    }
}
