package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.mapper.UserTagMapper;
import com.caiya.companion.model.domain.Tag;
import com.caiya.companion.model.domain.UserTag;
import com.caiya.companion.model.vo.TagVO;
import com.caiya.companion.service.TagService;
import com.caiya.companion.service.UserTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user_tag(用户标签关系表)】的数据库操作Service实现
* @createDate 2024-08-06 10:27:34
*/
@Service
public class UserTagServiceImpl extends ServiceImpl<UserTagMapper, UserTag>
    implements UserTagService {

//    @Resource
//    private TagService tagService;
//
//    @Override
//    public List<TagVO> getTagByUserId(Long userId) {
//        if (userId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id错误");
//        // 根据用户id查询用户所有的标签
//        QueryWrapper<UserTag> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("userId", userId);
//        // 所有的关联关系数组
//        List<UserTag> userTags = this.list(queryWrapper);
//        // 结果集
//        List<TagVO> res = new ArrayList<>();
//        // 信息脱敏
//        for (UserTag userTag : userTags) {
//            // 获取标签具体信息
//            Long tagId = userTag.getTagId();
//            Tag tag = tagService.getById(tagId);
//            // 信息脱敏
//            TagVO tagVO = new TagVO();
//            BeanUtils.copyProperties(tag, tagVO);
//            res.add(tagVO);
//        }
//        return res;
//    }
}




