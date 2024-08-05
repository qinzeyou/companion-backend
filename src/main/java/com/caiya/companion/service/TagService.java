package com.caiya.companion.service;

import com.caiya.companion.model.domain.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caiya.companion.model.domain.User;

/**
* @author Administrator
* @description 针对表【tag】的数据库操作Service
* @createDate 2024-06-15 19:43:24
*/
public interface TagService extends IService<Tag> {

    /**
     * 新增标签
     * @param tagName 标签名称
     * @param loginUser 当前登录用户
     * @return 新增标签的id
     */
    Long addTag(String tagName, User loginUser);
}
