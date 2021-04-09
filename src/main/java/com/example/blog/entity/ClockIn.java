package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.blog.common.base.BaseEntity;
import com.example.blog.enums.HeartValueEnum;
import lombok.Data;

/**
 * 签到
 *
 * @author 言曌
 * @date 2021/4/8 4:03 下午
 */
@Data
@TableName("clock_in")
public class ClockIn extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 心情值
     */
    private String heartValue;

    /**
     * 心情值
     */
    @TableField(exist = false)
    private String heartValueDesc;

    /**
     * 用户
     */
    @TableField(exist = false)
    private User user;

    /**
     * 获取描述值
     * @return
     */
    public String getHeartValueDesc() {
        return HeartValueEnum.getDescByCode(this.heartValue);
    }
}
