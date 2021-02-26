package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liuyanzhao
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 获得某个用户的粉丝用户ID
     *
     * @param userId
     * @return
     */
    List<Long> getFansUserIds(Long userId);

    /**
     * 关注的用户ID
     *
     * @param userId
     * @return
     */
    List<Long> getFollowUserIds(Long userId);

    /**
     * 根据用户ID和被关注用户ID查询
     *
     * @param userId
     * @param acceptUserId
     * @return
     */
    Follow getFollow(@Param("userId") Long userId,
                     @Param("acceptUserId") Long acceptUserId);
}

