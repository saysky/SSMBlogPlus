package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.ClockIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 言曌
 * @date 2021/4/8 4:08 下午
 */
@Mapper
public interface ClockInMapper extends BaseMapper<ClockIn> {

    ClockIn findByUserIdToday(Long userId);

    List<ClockIn> getRankToday();

    Integer countToday();

}
