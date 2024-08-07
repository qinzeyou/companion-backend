package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.mapper.TagMapper;
import com.caiya.companion.mapper.UserTagMapper;
import com.caiya.companion.model.domain.Tag;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.domain.UserTag;
import com.caiya.companion.model.enums.TagStatusEnum;
import com.caiya.companion.model.request.AddOrUpdateUserTagRequest;
import com.caiya.companion.model.vo.TagVO;
import com.caiya.companion.service.UserTagService;
import com.caiya.companion.utils.ColorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
* @author Administrator
* @description 针对表【user_tag(用户标签关系表)】的数据库操作Service实现
* @createDate 2024-08-06 10:27:34
*/
@Service
public class UserTagServiceImpl extends ServiceImpl<UserTagMapper, UserTag>
    implements UserTagService {

    @Resource
    private TagMapper tagMapper;
    @Resource
    private ColorUtils colorUtils;

    /**
     * 根据用户id获取用户标签
     * @param userId 用户id
     * @return 用户所有的标签列表
     */
    @Override
    public List<TagVO> getTagByUserId(Long userId) {
        if (userId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id错误");
        // 根据用户id查询用户所有的标签
        QueryWrapper<UserTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        // 所有的关联关系数组
        List<UserTag> userTags = this.list(queryWrapper);
        // 结果集
        List<TagVO> res = new ArrayList<>();
        // 信息脱敏
        for (UserTag userTag : userTags) {
            // 获取标签具体信息
            Long tagId = userTag.getTagId();
            Tag tag = tagMapper.selectById(tagId);
            // 信息脱敏
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            tagVO.setColor(userTag.getColor());
            tagVO.setTextColor(userTag.getTextColor());
            tagVO.setWeight(userTag.getWeight());
            res.add(tagVO);
        }
        return res;
    }

    /**
     * 用户添加自身标签
     *
     * @param request 请求体
     * @param loginUser         登录信息
     * @return 操作结果：true成功，false失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public Boolean addUserTag(AddOrUpdateUserTagRequest request, User loginUser) {
        // 统一校验，校验通过就获取 该条关联关系数据
        UserTag findUserTag = verifyParams(request, loginUser.getId());
        if (findUserTag != null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复添加标签");
        // 获取用户要添加的标签信息
        Tag tag = tagMapper.selectById(request.getTagId());
        try {
            // 数据库操作
            // 添加关系数据
            UserTag userTag = new UserTag();
            BeanUtils.copyProperties(request, userTag);
            userTag.setUserId(loginUser.getId());
            this.save(userTag);
            // 修改标签使用人数
            tag.setUserNumber(tag.getUserNumber() + 1);
            tagMapper.updateById(tag);
            return true;
        } catch (Exception e) {
            log.error("addUserTag err {}", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加标签失败");
        }
    }

    /**
     * 用户修改自身标签
     * @param request 请求体
     * @param loginUser 登录信息
     * @return 操作结果：true成功，false失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserTag(AddOrUpdateUserTagRequest request, User loginUser) {
        // 统一校验，校验通过就获取 该条关联关系数据
        UserTag findUserTag = verifyParams(request, loginUser.getId());
        try {
            UserTag userTag = new UserTag();
            BeanUtils.copyProperties(request, userTag);
            userTag.setUserId(loginUser.getId());
            userTag.setId(findUserTag.getId());
            this.updateById(userTag);
        } catch (Exception e) {
            log.error("updateUserTag error id {}", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改用户标签失败");
        }
        return true;
    }

    /**
     * 用户移除自己的标签（单个移除）
     * @param tagId 标签id
     * @param loginUser 登录信息
     * @return 操作结果：true成功，false失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeUserTag(Long tagId, User loginUser) {
        // 移除关联关系
        QueryWrapper<UserTag> userTagQueryWrapper = new QueryWrapper<>();
        userTagQueryWrapper.eq("tagId", tagId);
        userTagQueryWrapper.eq("userId", loginUser.getId());
        try {
            this.remove(userTagQueryWrapper);
        } catch (Exception e) {
            log.error("removeUserTag error is {}", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除用户标签失败");
        }
        return true;
    }

    /**
     * 校验【修改、添加】标签的请求体参数
     * @param request 请求体
     * @param userId 用户id
     * @return 校验通过返回该条 用户标签 关系数据
     */
    public UserTag verifyParams(AddOrUpdateUserTagRequest request, Long userId) {
        // 判断排序是否超出范围 1 - 100
        Integer weight = request.getWeight();
        if (weight <= 0 || weight > 100) throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签排序权重错误");
        // 文字颜色
        String textColor = request.getTextColor();
        // 背景颜色
        String color = request.getColor();
        // 判断【文字、背景】颜色
        if (StringUtils.isNotBlank(textColor) && !colorUtils.isValidHexColor(textColor))
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "颜色只能输入十六进制");
        if (StringUtils.isNotBlank(color) && !colorUtils.isValidHexColor(color))
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "颜色只能输入十六进制");
        // 获取标签信息
        Long tagId = request.getTagId();
        Tag tag = tagMapper.selectById(tagId);
        // 标签不存在，则抛出异常
        if (tag == null) throw new BusinessException(ErrorCode.NULL_ERROR, "标签不存在");
        // 标签状态需可见
        Integer status = tag.getStatus();
        if (Objects.equals(TagStatusEnum.NOT_VISIBLE.getValue(), status))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误的请求标签");
        // 只能添加子标签
        if (Boolean.TRUE.equals(tag.getIsParent()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能添加子标签");
        // 返回该条 用户标签 关系数据
        QueryWrapper<UserTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tagId", tagId);
        queryWrapper.eq("userId", userId);
        return this.getOne(queryWrapper);
    }
}

