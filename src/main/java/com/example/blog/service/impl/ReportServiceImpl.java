package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.dto.QueryCondition;
import com.example.blog.entity.Report;
import com.example.blog.mapper.ReportMapper;
import com.example.blog.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 言曌
 * @date 2021/2/21 5:30 下午
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public BaseMapper<Report> getRepository() {
        return reportMapper;
    }

    @Override
    public QueryWrapper<Report> getQueryWrapper(Report report) {
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        if (report != null) {
            if (report.getUserId() != null) {
                queryWrapper.eq("user_id", report.getUserId());
            }
            if (report.getPostId() != null) {
                queryWrapper.eq("post_id", report.getPostId());
            }
            if (report.getStatus() != null && report.getStatus() != -1) {
                queryWrapper.eq("status", report.getStatus());
            }
        }
        return queryWrapper;
    }


    @Override
    public List<Report> findByUserIdAndStatus(Long userId, Integer status) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("status", status);
        return reportMapper.selectList(queryWrapper);
    }
}
