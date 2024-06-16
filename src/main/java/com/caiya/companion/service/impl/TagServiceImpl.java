package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.model.domain.Tag;
import com.caiya.companion.service.TagService;
import com.caiya.companion.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @description 针对表【tag】的数据库操作Service实现
 * @createDate 2024-06-15 19:43:24
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {
}




