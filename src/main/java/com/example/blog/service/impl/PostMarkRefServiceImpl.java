package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.PostMarkRef;
import com.example.blog.mapper.PostMarkRefMapper;
import com.example.blog.service.PostMarkRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     文章收藏关联逻辑实现类
 * </pre>
 */
@Service
public class PostMarkRefServiceImpl implements PostMarkRefService {


    @Autowired
    private PostMarkRefMapper postMarkRefMapper;

    @Override
    public BaseMapper<PostMarkRef> getRepository() {
        return postMarkRefMapper;
    }

    @Override
    public QueryWrapper<PostMarkRef> getQueryWrapper(PostMarkRef postMarkRef) {
        //对指定字段查询
        QueryWrapper<PostMarkRef> queryWrapper = new QueryWrapper<>();
        if (postMarkRef != null) {
            if (postMarkRef.getUserId() != null && postMarkRef.getUserId() != -1) {
                queryWrapper.eq("user_id", postMarkRef.getUserId());
            }
            if (postMarkRef.getPostId() != null && postMarkRef.getPostId() != -1) {
                queryWrapper.eq("question_id", postMarkRef.getPostId());
            }
        }
        return queryWrapper;
    }

    @Override
    public PostMarkRef insertOrUpdate(PostMarkRef entity) {
        if (entity.getId() == null) {
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            insert(entity);
        } else {
            entity.setUpdateTime(new Date());
            update(entity);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        postMarkRefMapper.deleteById(id);
    }

    @Override
    public List<PostMarkRef> findAll() {
        List<PostMarkRef> postMarkRefList = postMarkRefMapper.selectList(null);
        return postMarkRefList;
    }

    @Override
    public Integer deleteByUserId(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        return postMarkRefMapper.deleteByMap(map);
    }
}
