package com.caiya.companion.controller;

import com.caiya.companion.common.*;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.AddUserTagRequest;
import com.caiya.companion.model.request.TagAddRequest;
import com.caiya.companion.model.request.TagUpdateRequest;
import com.caiya.companion.model.vo.TagTreeVO;
import com.caiya.companion.model.vo.TagVO;
import com.caiya.companion.service.TagService;
import com.caiya.companion.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author caiya
 * @description 标签控制层
 * @create 2024-08-05 21:21
 */
@RequestMapping("/tag")
@RestController
public class TagController {

    @Resource
    private TagService tagService;
    @Resource
    private UserService userService;

    /**
     * 新增标签
     * @param tagAddRequest 添加标签请求体
     * @return 新增标签的id
     */
    @PostMapping()
    private BaseResponse<Long> add(@RequestBody TagAddRequest tagAddRequest, HttpServletRequest request) {
        if (tagAddRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User loginUser = userService.getLoginUser(request);
        Long res = tagService.addTag(tagAddRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 删除标签
     * @param tagId 标签id
     * @param request 登录信息
     * @return 删除结果：true删除成功，false删除失败
     */
    @DeleteMapping("{id}")
    private BaseResponse<Boolean> del(@PathVariable("id") long tagId, HttpServletRequest request) {
        if (tagId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Boolean res = tagService.delTag(tagId, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 修改标签
     * PS：父标签不能改为子标签，因为这样很难处理父标签下的子标签
     * @param tagUpdateRequest 请求体
     * @param request 登录信息
     * @return 修改结果：true成功，false失败
     */
    @PutMapping()
    private BaseResponse<Boolean> update(@RequestBody TagUpdateRequest tagUpdateRequest, HttpServletRequest request) {
        if (tagUpdateRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        User loginUser = userService.getLoginUser(request);
        Boolean res = tagService.updateTag(tagUpdateRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     *
     * 获取树形结构的标签列表
     * @return 树形标签列表
     */
    @GetMapping("/tree")
    private BaseResponse<List<TagTreeVO>> treeTag() {
        List<TagTreeVO> res = tagService.treeTag();
        return ResultUtils.success(res);
    }

    /**
     * 用户添加自身标签
     * @param addUserTagRequest 请求体
     * @param request 登录信息
     * @return 操作结果：true成功，false失败
     */
    @PostMapping("/add/user/tag")
    private BaseResponse<Boolean> addUserTag(@RequestBody AddUserTagRequest addUserTagRequest, HttpServletRequest request) {
        if (addUserTagRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Boolean res = tagService.addUserTag(addUserTagRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 用户移除自己的标签（单个移除）
     * @param tagId 标签id
     * @param request 登录信息
     * @return 操作结果：true成功，false失败
     */
    @GetMapping("/remove/user/tag")
    private BaseResponse<Boolean> removeUserTag(@RequestParam("tagId") Long tagId, HttpServletRequest request) {
        if (tagId <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签id错误");
        User loginUser = userService.getLoginUser(request);
        Boolean res = tagService.removeUserTag(tagId, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 按照标签使用人数组成热门标签推荐，如果用户登录，则过滤掉用户已有的标签
     * @param pageRequest 请求体：分页参数
     * @param request 登录信息
     * @return 热门标签分页数据
     */
    @PostMapping("/hot/page")
    private BaseResponse<PageResponse<List<TagVO>>> hotTagPage(@RequestBody PageRequest pageRequest, HttpServletRequest request) {
        PageResponse<List<TagVO>> res = tagService.hotTagPage(pageRequest, request);
        return ResultUtils.success(res);
    }
}
