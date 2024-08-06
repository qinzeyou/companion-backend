package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.mapper.UserTagMapper;
import com.caiya.companion.model.domain.UserTag;
import com.caiya.companion.service.UserTagService;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user_tag(用户标签关系表)】的数据库操作Service实现
* @createDate 2024-08-06 10:27:34
*/
@Service
public class UserTagServiceImpl extends ServiceImpl<UserTagMapper, UserTag>
    implements UserTagService {

}




