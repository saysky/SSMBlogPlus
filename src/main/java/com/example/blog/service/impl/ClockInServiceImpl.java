package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.ClockIn;
import com.example.blog.mapper.ClockInMapper;
import com.example.blog.service.ClockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 言曌
 * @date 2021/4/8 4:55 下午
 */
@Service
public class ClockInServiceImpl implements ClockInService {

    @Autowired
    private ClockInMapper clockInMapper;

    @Override
    public BaseMapper<ClockIn> getRepository() {
        return clockInMapper;
    }

    @Override
    public QueryWrapper<ClockIn> getQueryWrapper(ClockIn clockIn) {
        QueryWrapper queryWrapper = new QueryWrapper();
        return queryWrapper;
    }

    @Override
    public ClockIn findByUserIdToday(Long userId) {
        return clockInMapper.findByUserIdToday(userId);
    }

    @Override
    public List<ClockIn> getRankToday() {
        List<ClockIn> clockInList = clockInMapper.getRankToday();
        return clockInList;
    }

    @Override
    public Integer countToday() {
        return clockInMapper.countToday();
    }

    @Override
    public Integer deleteByUserId(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        return clockInMapper.deleteByMap(map);
    }
}
