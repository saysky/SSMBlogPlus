package com.example.blog.service;


import com.example.blog.common.base.BaseService;
import com.example.blog.entity.Document;

/**
 * <pre>
 *     友情链接业务逻辑接口
 * </pre>
 *
 * @author saysky
 * @date 2021/3/20
 */
public interface DocumentService extends BaseService<Document, Long> {

    /**
     * 根据用户ID删除
     * @param userId
     * @return
     */
    Integer deleteByUserId(Long userId);
}
