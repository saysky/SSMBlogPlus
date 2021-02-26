package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.PostLikeRef;
import com.example.blog.mapper.PostLikeRefMapper;
import com.example.blog.service.PostLikeRefService;
import com.example.blog.service.PostLikeRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *     文章点赞关联逻辑实现类
 * </pre>
 */
@Service
public class PostLikeRefServiceImpl implements PostLikeRefService {


    @Autowired
    private PostLikeRefMapper postLikeRefMapper;

    @Override
    public BaseMapper<PostLikeRef> getRepository() {
        return postLikeRefMapper;
    }

    @Override
    public QueryWrapper<PostLikeRef> getQueryWrapper(PostLikeRef postLikeRef) {
        //对指定字段查询
        QueryWrapper<PostLikeRef> queryWrapper = new QueryWrapper<>();
        if (postLikeRef != null) {
            if (postLikeRef.getUserId() != null && postLikeRef.getUserId() != -1) {
                queryWrapper.eq("user_id", postLikeRef.getUserId());
            }
            if (postLikeRef.getPostId() != null && postLikeRef.getPostId() != -1) {
                queryWrapper.eq("question_id", postLikeRef.getPostId());
            }
        }
        return queryWrapper;
    }

    @Override
    public PostLikeRef insertOrUpdate(PostLikeRef entity) {
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
        postLikeRefMapper.deleteById(id);
    }

    @Override
    public List<PostLikeRef> findAll() {
        List<PostLikeRef> postLikeRefList = postLikeRefMapper.selectList(null);
        return postLikeRefList;
    }

}
