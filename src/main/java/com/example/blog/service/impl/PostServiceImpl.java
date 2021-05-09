package com.example.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.PostQueryCondition;
import com.example.blog.entity.*;
import com.example.blog.exception.MyBusinessException;
import com.example.blog.mapper.*;
import com.example.blog.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <pre>
 *     文章业务逻辑实现类
 * </pre>
 */
@Service
@Slf4j
public class PostServiceImpl implements PostService {


    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostCategoryRefMapper postCategoryRefMapper;

    @Autowired
    private PostTagRefMapper postTagRefMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostMarkRefMapper postMarkRefMapper;

    @Autowired
    private PostLikeRefMapper postLikeRefMapper;

    @Autowired
    private PostDisLikeRefMapper postDisLikeRefMapper;

    @Autowired
    private ReportMapper reportMapper;


    @Autowired
    private CommentMapper commentMapper;


    @Override
    @Async
    public void updatePostView(Long postId) {
        postMapper.incrPostViews(postId);
    }


    @Override
    public Long getTotalPostViews() {
        return postMapper.getPostViewsSum();
    }

    @Override
    public void resetCommentSize(Long postId) {
        postMapper.resetCommentSize(postId);
    }


    @Override
    public void deleteByUserId(Long userId) {
        postMapper.deleteByUserId(userId);
    }

    @Override
    public Page<Post> findPostByCondition(PostQueryCondition condition, Page<Post> page) {
        List<Post> postList = postMapper.findPostByCondition(condition, page);
        for(Post post : postList) {
            List<Tag> tagList = tagMapper.findByPostId(post.getId());
            post.setTagList(tagList);
        }
        return page.setRecords(postList);
    }

    @Override
    public BaseMapper<Post> getRepository() {
        return postMapper;
    }

    @Override
    public Post insert(Post post) {
        post.setPostViews(0L);
        post.setCommentSize(0L);
        post.setPostLikes(0L);
        postMapper.insert(post);
        //添加记录分类关系
        if (post.getCategory() != null) {
            postCategoryRefMapper.insert(new PostCategoryRef(post.getId(), post.getCategory().getId()));
        }
        //添加记录标签关系
        if (post.getTagList() != null) {
            for (int i = 0; i < post.getTagList().size(); i++) {
                postTagRefMapper.insert(new PostTagRef(post.getId(), post.getTagList().get(i).getId()));
            }
        }
        return post;
    }

    @Override
    public Post update(Post post) {
        postMapper.updateById(post);
        if (post.getCategory() != null) {
            //添加分类和记录关联
            postCategoryRefMapper.deleteByPostId(post.getId());
            //删除分类和记录关联
            PostCategoryRef postCategoryRef = new PostCategoryRef(post.getId(), post.getCategory().getId());
            postCategoryRefMapper.insert(postCategoryRef);
        }
        if (post.getTagList() != null && post.getTagList().size() != 0) {
            //删除标签和记录关联
            postTagRefMapper.deleteByPostId(post.getId());
            //添加标签和记录关联
            for (int i = 0; i < post.getTagList().size(); i++) {
                PostTagRef postTagRef = new PostTagRef(post.getId(), post.getTagList().get(i).getId());
                postTagRefMapper.insert(postTagRef);
            }
        }
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long postId) {
        Post post = this.get(postId);
        if (post != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("post_id", postId);

            postMapper.deleteById(post.getId());
            // 删除收藏
            postMarkRefMapper.deleteByMap(map);
            // 删除点赞
            postLikeRefMapper.deleteByMap(map);
            // 删除点踩
            postDisLikeRefMapper.deleteByMap(map);
            // 删除标签关联
            postTagRefMapper.deleteByMap(map);
            // 删除分类关联
            postCategoryRefMapper.deleteByMap(map);
            // 删除评论
            commentMapper.deleteByMap(map);
            // 删除反馈
            reportMapper.deleteByMap(map);
        }
    }

    @Override
    public QueryWrapper<Post> getQueryWrapper(Post post) {
        //对指定字段查询
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (post != null) {
            if (StrUtil.isNotBlank(post.getPostTitle())) {
                queryWrapper.like("post_title", post.getPostTitle());
            }
            if (StrUtil.isNotBlank(post.getPostContent())) {
                queryWrapper.like("post_content", post.getPostContent());
            }
            if (post.getUserId() != null && post.getUserId() != -1) {
                queryWrapper.eq("user_id", post.getUserId());
            }
            if (post.getPostStatus() != null && post.getPostStatus() != -1) {
                queryWrapper.eq("post_status", post.getPostStatus());
            }
            if (StringUtils.isNotEmpty(post.getPostType())) {
                queryWrapper.eq("post_type", post.getPostType());
            }
        }
        return queryWrapper;
    }

    @Override
    public Post insertOrUpdate(Post post) {
        if (post.getId() == null) {
            insert(post);
        } else {
            update(post);
        }
        return post;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addLike(Long postId, User user) {
        QueryWrapper<PostLikeRef> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("post_id", postId);
        List<PostLikeRef> postLikeRefs = postLikeRefMapper.selectList(queryWrapper);
        if (postLikeRefs != null && postLikeRefs.size() > 0) {
            throw new MyBusinessException("您已经点赞过了");
        }


        // 更新
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new MyBusinessException("问题不存在");
        }
        post.setPostLikes(post.getPostLikes() + 1);
        postMapper.updateById(post);

        // 添加点赞关联
        PostLikeRef postLikeRef = new PostLikeRef();
        postLikeRef.setUserId(user.getId());
        postLikeRef.setPostId(postId);
        postLikeRef.setCreateTime(new Date());
        postLikeRef.setUpdateTime(new Date());
        postLikeRef.setCreateBy(user.getUserName());
        postLikeRef.setUpdateBy(user.getUserName());
        postLikeRefMapper.insert(postLikeRef);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDisLike(Long postId, User user) {
        QueryWrapper<PostDisLikeRef> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("post_id", postId);
        List<PostDisLikeRef> postDisLikeRefs = postDisLikeRefMapper.selectList(queryWrapper);
        if (postDisLikeRefs != null && postDisLikeRefs.size() > 0) {
            throw new MyBusinessException("您已经点踩过了");
        }


        // 更新
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new MyBusinessException("问题不存在");
        }
        post.setPostDisLikes(post.getPostDisLikes() + 1);
        postMapper.updateById(post);

        // 添加点赞关联
        PostDisLikeRef postDisLikeRef = new PostDisLikeRef();
        postDisLikeRef.setUserId(user.getId());
        postDisLikeRef.setPostId(postId);
        postDisLikeRef.setCreateTime(new Date());
        postDisLikeRef.setUpdateTime(new Date());
        postDisLikeRef.setCreateBy(user.getUserName());
        postDisLikeRef.setUpdateBy(user.getUserName());
        postDisLikeRefMapper.insert(postDisLikeRef);

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMark(Long postId,  User user) {

        QueryWrapper<PostMarkRef> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("post_id", postId);
        List<PostMarkRef> postMarkRefs = postMarkRefMapper.selectList(queryWrapper);
        if (postMarkRefs != null && postMarkRefs.size() > 0) {
            throw new MyBusinessException("您已经收藏过了");
        }

        // 更新
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new MyBusinessException("问题不存在");
        }
        post.setMarkCount(post.getMarkCount() + 1);
        postMapper.updateById(post);

        // 添加收藏关联
        PostMarkRef postMarkRef = new PostMarkRef();
        postMarkRef.setUserId(user.getId());
        postMarkRef.setPostId(postId);
        postMarkRef.setCreateTime(new Date());
        postMarkRef.setUpdateTime(new Date());
        postMarkRef.setCreateBy(user.getUserName());
        postMarkRef.setUpdateBy(user.getUserName());
        postMarkRefMapper.insert(postMarkRef);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMark(Long postId, User user) {
        // 删除关联
        QueryWrapper<PostMarkRef> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("post_id", postId);
        postMarkRefMapper.delete(queryWrapper);

        // 更新
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new MyBusinessException("问题不存在");
        }
        post.setMarkCount(post.getMarkCount() - 1);
        postMapper.updateById(post);
    }
}

