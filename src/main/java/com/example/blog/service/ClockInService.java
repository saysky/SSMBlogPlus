package com.example.blog.service;

import com.example.blog.common.base.BaseService;
import com.example.blog.entity.ClockIn;

import java.util.List;

/**
 * @author 言曌
 * @date 2021/4/8 4:53 下午
 */

public interface ClockInService extends BaseService<ClockIn, Long> {

    /**
     * 查询今天的
     *
     * @param userId
     * @return
     */
    ClockIn findByUserIdToday(Long userId);

    /**
     * 获得今天的签到排名
     *
     * @return
     */
    List<ClockIn> getRankToday();

    /**
     * 统计今天的签单数
     *
     * @return
     */
    Integer countToday();

    /**
     * 根据用户ID删除
     * @param userId
     * @return
     */
    Integer deleteByUserId(Long userId);
}
