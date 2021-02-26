package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import lombok.Data;

/**
 * 举报反馈
 *
 * @author 言曌
 * @date 2021/2/21 5:27 下午
 */

@Data
@TableName("report")
public class Report extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 所属文章ID
     */
    private Long postId;

    /**
     * 举报内容
     */
    private String content;

    /**
     * 处理回复
     */
    private String remark;

    /**
     * 处理状态:0待处理，1已处理
     */
    private Integer status;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Post post;
}
