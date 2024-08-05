package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.Tag;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.service.TagService;
import com.caiya.companion.mapper.TagMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @description 针对表【tag】的数据库操作Service实现
 * @createDate 2024-06-15 19:43:24
 */
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

    /**
     * 新增标签
     * @param tagName 标签名称
     * @param loginUser 当前登录用户
     * @return 新增标签的id
     */
    @Override
    public Long addTag(String tagName, User loginUser) {
        // 标签名称长度 <= 6
        if (tagName.length() > 6) throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称长度过长");
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setUserId(loginUser.getId());
        try {
            this.save(tag);
        } catch (Exception e) {
            log.error("add Tag {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增标签失败");
        }
        return tag.getId();
    }
}