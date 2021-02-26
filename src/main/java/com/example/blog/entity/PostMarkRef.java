package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import com.example.blog.util.RelativeDateFormat;
import lombok.Data;

/**
 * 文章收藏关联
 *
 * @author 言曌
 * @date 2021/2/21 12:01 下午
 */

@Data
@TableName("post_mark_ref")
public class PostMarkRef extends BaseEntity {

    /**
     * 收藏人ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 文章
     */
    @TableField(exist = false)
    private Post post;

    @TableField(exist = false)
    private User user;


    /**
     * 创建时间
     */
    @TableField(exist = false)
    private String createTimeStr;

    public String getCreateTimeStr() {
        return RelativeDateFormat.format(getCreateTime());
    }
}
