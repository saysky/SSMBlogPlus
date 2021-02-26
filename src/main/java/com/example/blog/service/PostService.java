package com.example.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.base.BaseService;
import com.example.blog.dto.PostQueryCondition;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;

/**
 * <pre>
 *     记录/页面业务逻辑接口
 * </pre>
 */
public interface PostService extends BaseService<Post, Long> {

    /**
     * 修改记录阅读量
     *
     * @param postId 记录Id
     * @return 记录访问量
     */
    void updatePostView(Long postId);

    /**
     * 获取所有记录的阅读量
     *
     * @return Long
     */
    Long getTotalPostViews();

    /**
     * 更新记录回复数
     *
     * @param postId 记录Id
     */
    void resetCommentSize(Long postId);

    /**
     * 删除用户的记录
     *
     * @param userId 用户Id
     */
    void deleteByUserId(Long userId);

    /**
     * 根据条件获得列表
     *
     * @param condition
     * @return
     */
    Page<Post> findPostByCondition(PostQueryCondition condition, Page<Post> page);

    /**
     * 点赞
     *
     * @param postId
     * @param user
     */
    void addLike(Long postId, User user);

    /**
     * 点踩
     *
     * @param postId
     * @param user
     */
    void addDisLike(Long postId, User user);


    /**
     * 收藏
     *
     * @param postId
     * @param user
     */
    void addMark(Long postId, User user);

    /**
     * 删除收藏
     *
     * @param postId
     * @param user
     */
    void deleteMark(Long postId, User user);

}
