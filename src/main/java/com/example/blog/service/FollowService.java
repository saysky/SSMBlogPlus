package com.example.blog.service;


import com.example.blog.common.base.BaseService;
import com.example.blog.entity.Follow;
import com.example.blog.entity.User;

import java.util.List;


/**
 * <pre>
 *    关注业务逻辑接口
 * </pre>
 */
public interface FollowService extends BaseService<Follow, Long> {


    /**
     * 根据用户ID和被关注用户ID查询
     *
     * @param userId
     * @param acceptUserId
     * @return
     */
    Follow findByUserIdAndAcceptUserId(Long userId, Long acceptUserId);


    /**
     * 根据用户ID查询
     *
     * @param userId
     * @return
     */
    List<Follow> findByUserId(Long userId);

    /**
     * 查询互粉用户ID列表
     *
     * @param userId
     * @return
     */
    List<Long> findMutualFollowingByUserId(Long userId);

    /**
     * 关注用户
     *
     * @param user
     * @param acceptUserId
     * @return
     */
    void follow(User user, Long acceptUserId);

    /**
     * 取关用户
     *
     * @param user
     * @param acceptUserId
     * @return
     */
    void unfollow(User user, Long acceptUserId);

    /**
     * 判断两个用户是否互相关注
     *
     * @param fromUserId
     * @param toUserId
     * @return
     */
    boolean isMutualFollowing(Long fromUserId, Long toUserId);

    /*
     * 根据用户ID删除
     * @param userId
     * @return
     */
    Integer deleteByUserId(Long userId);

}
