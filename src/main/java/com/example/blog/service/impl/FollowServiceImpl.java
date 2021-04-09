package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Follow;
import com.example.blog.entity.User;
import com.example.blog.mapper.FollowMapper;
import com.example.blog.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <pre>
 *     关注业务逻辑实现类
 * </pre>
 */
@Service
public class FollowServiceImpl implements FollowService {


    @Autowired
    private FollowMapper followMapper;

    @Override
    public BaseMapper<Follow> getRepository() {
        return followMapper;
    }

    @Override
    public QueryWrapper<Follow> getQueryWrapper(Follow follow) {
        //对指定字段查询
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        if (follow != null) {
            if (follow.getUserId() != null && follow.getUserId() != -1) {
                queryWrapper.eq("user_id", follow.getUserId());
            }
            if (follow.getAcceptUserId() != null && follow.getAcceptUserId() != -1) {
                queryWrapper.eq("accept_user_id", follow.getAcceptUserId());
            }
        }
        return queryWrapper;
    }

    @Override
    public Follow insertOrUpdate(Follow entity) {
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
        followMapper.deleteById(id);
    }

    @Override
    public List<Follow> findAll() {
        List<Follow> followList = followMapper.selectList(null);
        return followList;
    }

    @Override
    public Follow findByUserIdAndAcceptUserId(Long userId, Long acceptUserId) {
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("accept_user_id", acceptUserId);
        List<Follow> follows = followMapper.selectList(queryWrapper);
        if (follows != null && follows.size() > 0) {
            return follows.get(0);
        }
        return null;
    }

    @Override
    public List<Follow> findByUserId(Long userId) {
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return followMapper.selectList(queryWrapper);
    }

    @Override
    public List<Long> findMutualFollowingByUserId(Long userId) {
        List<Long> fans = followMapper.getFansUserIds(userId);
        List<Long> follows = followMapper.getFollowUserIds(userId);
        fans.retainAll(follows);
        return fans;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(User user, Long acceptUserId) {
        // 先删除关系
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("accept_user_id", acceptUserId);
        followMapper.delete(queryWrapper);

        // 再添加关系
        Follow follow = new Follow();
        follow.setCreateTime(new Date());
        follow.setUpdateTime(new Date());
        follow.setUserId(user.getId());
        follow.setAcceptUserId(acceptUserId);
        follow.setUpdateBy(user.getUserName());
        follow.setCreateBy(user.getUserName());
        followMapper.insert(follow);
    }

    @Override
    public void unfollow(User user, Long acceptUserId) {
        // 删除关系
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("accept_user_id", acceptUserId);
        followMapper.delete(queryWrapper);
    }

    @Override
    public boolean isMutualFollowing(Long fromUserId, Long toUserId) {
        if (Objects.equals(fromUserId, toUserId)) {
            return true;
        }
        Follow follow = followMapper.getFollow(fromUserId, toUserId);
        Follow follow2 = followMapper.getFollow(toUserId, fromUserId);
        return follow != null && follow2 != null;
    }

    @Override
    public Integer deleteByUserId(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        int num1 = followMapper.deleteByMap(map);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("accept_user_id", userId);
        int num2 = followMapper.deleteByMap(map2);
        return num1 + num2;
    }
}
