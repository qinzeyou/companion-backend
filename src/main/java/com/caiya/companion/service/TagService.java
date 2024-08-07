package com.caiya.companion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caiya.companion.common.PageRequest;
import com.caiya.companion.common.PageResponse;
import com.caiya.companion.model.domain.Tag;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.TagAddRequest;
import com.caiya.companion.model.request.TagUpdateRequest;
import com.caiya.companion.model.vo.TagTreeVO;
import com.caiya.companion.model.vo.TagVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Administrator
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2024-08-06 10:20:35
*/
public interface TagService extends IService<Tag> {

    /**
     * 新增标签
     * @param tagAddRequest 添加标签请求体
     * @return 新增标签的id
     */
    Long addTag(TagAddRequest tagAddRequest, User loginUser);

    /**
     * 删除标签
     * @param tagId 标签id
     * @param loginUser 登录信息
     * @return 删除结果：true删除成功，false删除失败
     */
    Boolean delTag(long tagId, User loginUser);

    /**
     * 修改标签
     * PS：父标签不能改为子标签，因为这样很难处理父标签下的子标签
     * @param tagUpdateRequest 请求体
     * @param loginUser 登录信息
     * @return @return 修改结果：true成功，false失败
     */
    Boolean updateTag(TagUpdateRequest tagUpdateRequest, User loginUser);

    /**
     * 获取树形结构的标签列表
     * @return 树形标签列表
     */
    List<TagTreeVO> treeTag();

    /**
     * 按照标签使用人数组成热门标签推荐，如果用户登录，则过滤掉用户已有的标签
     * @param pageRequest 请求体：分页参数
     * @param request 登录信息
     * @return 热门标签分页数据
     */
    PageResponse<List<TagVO>> hotTagPage(PageRequest pageRequest, HttpServletRequest request);
}
