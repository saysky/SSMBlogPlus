package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Document;
import com.example.blog.mapper.DocumentMapper;
import com.example.blog.service.DocumentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     资源业务逻辑实现类
 * </pre>
 */
@Service
public class DocumentServiceImpl implements DocumentService {


    @Autowired
    private DocumentMapper documentMapper;

    @Override
    public BaseMapper<Document> getRepository() {
        return documentMapper;
    }

    @Override
    public QueryWrapper<Document> getQueryWrapper(Document document) {
        //对指定字段查询
        QueryWrapper<Document> queryWrapper = new QueryWrapper<>();
        if (document != null) {
            if (document.getUserId() != null && document.getUserId() != -1) {
                queryWrapper.eq("user_id", document.getUserId());
            }
            if (StringUtils.isNotEmpty(document.getName())) {
                queryWrapper.like("name", document.getName());
            }
        }
        return queryWrapper;
    }

    @Override
    public Document insertOrUpdate(Document entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else {
            update(entity);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        documentMapper.deleteById(id);
    }

    @Override
    public List<Document> findAll() {
        List<Document> documentList = documentMapper.selectList(null);
        return documentList;
    }

    @Override
    public Integer deleteByUserId(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        return documentMapper.deleteByMap(map);
    }
}
