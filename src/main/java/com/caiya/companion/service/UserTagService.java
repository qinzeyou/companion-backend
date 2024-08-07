package com.caiya.companion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.domain.UserTag;
import com.caiya.companion.model.request.AddOrUpdateUserTagRequest;
import com.caiya.companion.model.vo.TagVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【user_tag(用户标签关系表)】的数据库操作Service
* @createDate 2024-08-06 10:27:34
*/
public interface UserTagService extends IService<UserTag> {
    /**
     * 根据用户id获取用户标签
     * @param userId 用户id
     * @return 用户所有的标签列表
     */
    List<TagVO> getTagByUserId(Long userId);

    /**
     * 用户添加自身标签
     * @param addUserTagRequest 请求体
     * @param loginUser 登录信息
     * @return 操作结果：true成功，false失败
     */
    Boolean addUserTag(AddOrUpdateUserTagRequest addUserTagRequest, User loginUser);

    /**
     * 用户修改自身标签
     * @param addUserTagRequest 请求体
     * @param loginUser 登录信息
     * @return 操作结果：true成功，false失败
     */
    Boolean updateUserTag(AddOrUpdateUserTagRequest addUserTagRequest, User loginUser);

    /**
     * 用户移除自己的标签（单个移除）
     * @param tagId 标签id
     * @param loginUser 登录信息
     * @return 操作结果：true成功，false失败
     */
    Boolean removeUserTag(Long tagId, User loginUser);
}
