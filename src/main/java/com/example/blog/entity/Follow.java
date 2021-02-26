package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import lombok.Data;

/**
 * <pre>
 *     关注
 * </pre>
 */
@Data
@TableName("follow")
public class Follow extends BaseEntity {

    /**
     * 关注者用户ID
     */
    private Long userId;

    /**
     * 被关注者用户ID
     */
    private Long acceptUserId;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private User acceptUser;

    /**
     * 是否互相关注
     */
    @TableField(exist = false)
    private Boolean isMutualFollowing;
}
