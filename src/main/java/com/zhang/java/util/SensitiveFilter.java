package com.zhang.java.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Date 2022/4/13 19:32
 * @Author zsy
 * @Description 使用前缀树过滤敏感词
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 敏感词替换
    private static final String REPLACEMENT_WORDS = "***";

    // 前缀树
    private TrieNode root = new TrieNode();


    /**
     * ioc容器实例化这个bean时，调用该方法
     */
    @PostConstruct
    public void init() {
        InputStream is = null;
        BufferedReader br = null;
        String sensitiveWord;

        try {
            is = getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            br = new BufferedReader(new InputStreamReader(is));
            while ((sensitiveWord = br.readLine()) != null) {
                addSensitiveWord(sensitiveWord);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 前缀树添加敏感词
     *
     * @param sensitiveWord
     */
    private void addSensitiveWord(String sensitiveWord) {
        TrieNode node = root;

        for (int i = 0; i < sensitiveWord.length(); i++) {
            char c = sensitiveWord.charAt(i);
            //得到当前节点的子节点
            TrieNode subNode = node.getSubNode(c);

            //如果当前节点的子节点为空，创建子节点，并在当前节点中添加子节点
            if (subNode == null) {
                subNode = new TrieNode();
                node.addSubNode(c, subNode);
            }

            //当前节点指向子节点
            node = subNode;
        }

        //设置当前节点为叶节点，即表示一个完整的敏感词
        node.setSensitiveWordEnd(true);
    }

    /**
     * 过滤敏感词，并进行替换
     *
     * @param text 要被过滤的文本
     * @return 过滤敏感词之后的文本
     */
    public String filterSensitiveWords(String text) {
        if (text == null || text.length() == 0) {
            return text;
        }

        //
        int left;
        int right;
        StringBuilder sb = new StringBuilder();

        while (left < text.length()) {

        }

        return sb;
    }

    /**
     * 前缀树节点
     */
    private class TrieNode {
        // 当前节点的子节点集合，key是下级字符，value是下级节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        // 记录当前节点是否是叶节点，即从根节点到叶节点是一个完整的敏感词
        private Boolean sensitiveWordEnd = false;

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            subNodes.get(c);
        }

        public Boolean getSensitiveWordEnd() {
            return sensitiveWordEnd;
        }

        public void setSensitiveWordEnd(Boolean sensitiveWordEnd) {
            this.sensitiveWordEnd = sensitiveWordEnd;
        }
    }
}
