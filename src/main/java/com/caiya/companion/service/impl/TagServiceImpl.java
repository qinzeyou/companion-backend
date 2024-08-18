package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.common.PageRequest;
import com.caiya.companion.common.PageResponse;
import com.caiya.companion.constant.SystemConstant;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.mapper.TagMapper;
import com.caiya.companion.mapper.UserTagMapper;
import com.caiya.companion.model.domain.Tag;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.domain.UserTag;
import com.caiya.companion.model.enums.TagStatusEnum;
import com.caiya.companion.model.qo.TagSearchListQO;
import com.caiya.companion.model.request.TagAddRequest;
import com.caiya.companion.model.request.TagUpdateRequest;
import com.caiya.companion.model.vo.TagTreeVO;
import com.caiya.companion.model.vo.TagVO;
import com.caiya.companion.model.vo.UserVO;
import com.caiya.companion.service.TagService;
import com.caiya.companion.utils.ColorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.caiya.companion.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author Administrator
 * @description 针对表【tag(标签表)】的数据库操作Service实现
 * @createDate 2024-08-06 10:20:35
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

    @Autowired
    private ColorUtils colorUtils;
    @Resource
    private UserTagMapper userTagMapper;
    @Resource
    private TagMapper tagMapper;


    /**
     * 新增标签
     * 父标签不能存在文字颜色和背景颜色（自觉遵守）
     *
     * @param tagAddRequest 添加标签请求体
     * @return 新增标签的id
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public Long addTag(TagAddRequest tagAddRequest, User loginUser) {
        // 校验请求参数
        // 标签名称 <= 10
        if (tagAddRequest.getTagName().length() > 10)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称过长");
        //   标签名称不能重复
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tagName", tagAddRequest.getTagName());
        Tag findTag = this.getOne(queryWrapper);
        if (findTag != null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称重复");
        // 不是父标签 and 校验是否为合格的十六进制颜色字符串
        // 文字颜色
        String textColor = tagAddRequest.getTextColor();
        // 背景颜色
        String color = tagAddRequest.getColor();
        // 不是父标签
        if (Boolean.FALSE.equals(tagAddRequest.getIsParent())) {
            // 传入的文字颜色不为空 and 颜色格式不正确
            if (StringUtils.isNotBlank(textColor) && !colorUtils.isValidHexColor(textColor))
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "颜色只能输入十六进制");
            // 传入的背景颜色不为空 and 颜色格式不正确
            if (StringUtils.isNotBlank(color) && !colorUtils.isValidHexColor(color))
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "颜色只能输入十六进制");
            // 如果没有传入颜色，则设置默认颜色
            tagAddRequest.setTextColor(textColor != null ? textColor : SystemConstant.TAG_TEXT_COLOR);
            tagAddRequest.setColor(textColor != null ? textColor : SystemConstant.TAG_COLOR);
            // 标签的父标签id为空，则默认存到 父标签【其他】
            if (tagAddRequest.getParentId() == null) {
                QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
                tagQueryWrapper.eq("tagName", "其他");
                Tag tag = this.getOne(tagQueryWrapper);
                tagAddRequest.setParentId(tag.getId());
            }
        }
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagAddRequest, tag);
        tag.setUserId(loginUser.getId());
        this.save(tag);
        return tag.getId();
    }

    /**
     * 删除标签
     *
     * @param tagId     标签id
     * @param loginUser 登录信息
     * @return 删除结果：true删除成功，false删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public Boolean delTag(long tagId, User loginUser) {
        // 校验请求参数
        // 标签是否存在
        Tag tag = this.getById(tagId);
        if (tag == null) throw new BusinessException(ErrorCode.NULL_ERROR, "没有该标签的信息");
        // 待删除的标签id数组
        List<Long> childrenTagList = new ArrayList<>(Collections.singletonList(tagId));
        //  如果删除的是父标签
        if (Boolean.TRUE.equals(tag.getIsParent())) {
            // 查询父标签下的所有标签id
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("parentId", tagId);
            // 将父标签下子标签id数组合并
            childrenTagList.addAll(this.list(tagQueryWrapper).stream().map(Tag::getId).collect(Collectors.toList()));
        }
        boolean res;
        try {
            // 删除用户标签关联关系
            QueryWrapper<UserTag> userTagQueryWrapper = new QueryWrapper<>();
            userTagQueryWrapper.in("tagId", childrenTagList);
            userTagMapper.delete(userTagQueryWrapper);
            // 删除标签
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.in("id", childrenTagList);
            res = this.remove(tagQueryWrapper);
        } catch (Exception e) {
            log.error("delTag err {}", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除标签失败");
        }
        return res;
    }

    /**
     * 修改标签
     * PS：父标签不能改为子标签，因为这样很难处理父标签下的子标签
     *
     * @param tagUpdateRequest 请求体
     * @param loginUser        登录信息
     * @return 修改结果：true成功，false失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 保证事务的原子性，语句要么都执行成功，要么都不成功
    public Boolean updateTag(TagUpdateRequest tagUpdateRequest, User loginUser) {
        // 校验请求参数
        //   标签名称 <= 10
        if (tagUpdateRequest.getTagName().length() > 10)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称过长");
        //   标签名称不能重复
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tagName", tagUpdateRequest.getTagName());
        Tag findTag = this.getOne(queryWrapper);
        if (findTag != null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称重复");
        // 不是父标签 and 校验是否为合格的十六进制颜色字符串
        // 文字颜色
        String textColor = tagUpdateRequest.getTextColor();
        // 背景颜色
        String color = tagUpdateRequest.getColor();
        // 如果是子标签则需要对颜色进行校验
        if (Boolean.FALSE.equals(tagUpdateRequest.getIsParent())) {
            if (StringUtils.isNotBlank(textColor) && !colorUtils.isValidHexColor(textColor))
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "颜色只能输入十六进制");

            if (StringUtils.isNotBlank(color) && !colorUtils.isValidHexColor(color))
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "颜色只能输入十六进制");
            // 如果没有传入颜色，则设置默认颜色
            tagUpdateRequest.setTextColor(textColor != null ? textColor : SystemConstant.TAG_TEXT_COLOR);
            tagUpdateRequest.setColor(textColor != null ? textColor : SystemConstant.TAG_COLOR);
        }
        // 标签是否存在
        Tag tag = this.getById(tagUpdateRequest.getId());
        if (tag == null) throw new BusinessException(ErrorCode.NULL_ERROR, "待修改的标签不存在");
        // 修改标签
        Tag upadteTag = new Tag();
        BeanUtils.copyProperties(tagUpdateRequest, upadteTag);
        boolean save;
        try {
            save = this.updateById(upadteTag);
        } catch (Exception e) {
            log.error("updateTag err {}", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改标签信息失败");
        }
        return save;
    }

    /**
     * 获取树形结构的标签列表
     *
     * @return 树形标签列表
     */
    @Override
    public List<TagTreeVO> treeTag() {
        // 获取所有标签
        List<Tag> tagList = this.list();
        // 结果
        List<TagTreeVO> res = new ArrayList<>();

        // 把查询出来的标签列表 解构成 map结构 key：父标签 value：子标签数组
        Map<Tag, List<Tag>> map = new HashMap<>();
        for (Tag tag : tagList) {
            // 标签状态
            Integer status = tag.getStatus();
            // 子标签 和 标签状态不可见 跳过
            if (Boolean.FALSE.equals(tag.getIsParent()) || Objects.equals(TagStatusEnum.NOT_VISIBLE.getValue(), status))
                continue;
            ArrayList<Tag> childrenTagList = new ArrayList<>();
            for (Tag children : tagList) {
                // 遍历标签列表，找到标签的 父id 与 当前标签id 相等，也就是有父子关系
                if (Objects.equals(children.getParentId(), tag.getId())) childrenTagList.add(children);
            }
            map.put(tag, childrenTagList);
        }
        // 信息脱敏，并且将 map -> tree
        for (Map.Entry<Tag, List<Tag>> entry : map.entrySet()) {
            // 父标签
            Tag tag = entry.getKey();
            // 标签信息脱敏
            TagTreeVO tagVO = new TagTreeVO();
            BeanUtils.copyProperties(tag, tagVO);
            // 子标签数组进行脱敏
            List<TagVO> childrenTagList = new ArrayList<>();
            for (Tag t : entry.getValue()) {
                TagVO vo = new TagVO();
                BeanUtils.copyProperties(t, vo);
                childrenTagList.add(vo);
            }
            // 将脱敏后的子标签数组存到父标签的 children 属性
            tagVO.setChildren(childrenTagList);
            res.add(tagVO);
        }
        return res;
    }

    /**
     * 按照标签使用人数组成热门标签推荐，如果用户登录，则过滤掉用户已有的标签
     *
     * @param pageRequest 请求体：分页参数
     * @param request     登录信息
     * @return 热门标签分页数据
     */
    @Override
    public PageResponse<List<TagVO>> hotTagPage(PageRequest pageRequest, HttpServletRequest request) {
        // 获取用户信息
        // 从session中获取用户信息
        UserVO user = (UserVO) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 未登录：设置为0：按照标签使用人数查询标签
        // 已登录：设置为用户id，过滤该用户已有的标签
        Long userId = user == null ? 0L : user.getId();
        // 分页参数
        int pageNum = pageRequest.getPageNum(); // 页码
        int pageSize = pageRequest.getPageSize(); // 每页条数
        Integer offset = (pageNum - 1) * pageSize; // 计算SQL分页查询起始位置
        List<Tag> tags = tagMapper.hotTagPage(offset, pageSize, userId);
        List<TagVO> tagVOList = tags.stream().map(tag -> {
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            return tagVO;
        }).collect(Collectors.toList());
        // 封装分页结果集
        PageResponse<List<TagVO>> response = new PageResponse<>();
        response.setRecords(tagVOList);
        response.setSize(pageSize);
        response.setCurrent(pageNum);
        response.setSize(tags.size());
        return response;
    }

    /**
     * 条件查询标签，如果请求体为空，则默认查询全部
     * @param tagSearchListQO 查询请求体
     * @return 符合查询条件的标签数组
     */
    @Override
    public List<TagVO> searchTagList(TagSearchListQO tagSearchListQO) {
        // 查询条件
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        // 只查询子标签
        queryWrapper.eq("isParent", 0);
        // 如果条件不为为空才拼接查询条件
        if (tagSearchListQO != null && StringUtils.isNotEmpty(tagSearchListQO.getTagName())){
            // and连接符，查询子标签且模糊查询传入的查询字符串
            queryWrapper.and(qw -> qw.like("tagName", tagSearchListQO.getTagName()));
        }
        // 查询出的结果
        List<Tag> list = tagMapper.selectList(queryWrapper);
        // 处理数据，整理成结果集所需数据
        List<TagVO> res = new ArrayList<>();
        for (Tag tag : list) {
            // 标签信息处理
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            res.add(tagVO);
        }
        return res;
    }
}