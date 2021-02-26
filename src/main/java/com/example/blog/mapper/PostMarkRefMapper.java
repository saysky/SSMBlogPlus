package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.PostMarkRef;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章收藏关联mapper
 * @author 言曌
 * @date 2020/10/31 12:47 下午
 */
@Mapper
public interface PostMarkRefMapper extends BaseMapper<PostMarkRef> {

}
