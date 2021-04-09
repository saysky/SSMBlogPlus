package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import com.example.blog.util.RelativeDateFormat;
import lombok.Data;

/**
 * 资源
 *
 * @author saysky
 * @date 2021/3/20
 */
@Data
@TableName("document")
public class Document extends BaseEntity {

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源URL
     */
    private String url;

    /**
     * 文件大小
     */
    private String size;

    /**
     * 物理路径
     */
    private String path;

    /**
     * 后缀
     */
    private String suffix;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 下载次数
     */
    private Integer downloadNum;

    /**
     * 用户
     */
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
